(ns schema
  (:require [com.walmartlabs.lacinia.schema :as schema]
            [com.walmartlabs.lacinia.util :as util]
            [com.walmartlabs.lacinia.executor :as executor]
            [com.walmartlabs.lacinia :as graphql]
            [com.walmartlabs.lacinia.resolve :as resolve]
            [honeysql.core :as sql]
            [honeysql.helpers :as h]
            [clojure.java.jdbc :as jdbc]
            [manifold.deferred :as d]
            [grouper.core :as grouper]))


; SQL Connection

(def db "postgresql://localhost:5432/graphql_batching")


;; Batching Library

(defn build-resolver
  [ctx batch args resolve-fn opts]
  (fn [values]
    (when (:debug opts)
      (println (format "Batching %d values to %s" (count values) batch)))
    (resolve-fn ctx args values)))


(defn build-batcher-opts
  [opts]
  (as-> opts $
    (merge {:interval 1 :capacity 500} $)
    (select-keys $ [:interval :capacity])
    (apply concat $)))


(defn init-batcher
  [{:keys [batches] :as ctx} batch args resolve-fn opts]
  (let [batcher-opts (build-batcher-opts opts)]
    (or (get @batches batch)
        (let [resolver (build-resolver ctx batch args resolve-fn opts)
              batcher (apply grouper/start! resolver batcher-opts)]
          (swap! batches assoc batch batcher)
          batcher))))


(defn on-batch-error
  [result batch]
  (fn [error]
    (let [info {:batch batch :message (str "Exception: " (.getMessage error))}]
      (resolve/deliver! result nil info))))


(defn with-batching
  [resolve-fn & {:keys [interval max-capacity] :as opts}]
  (fn [{:keys [batches ::graphql/selection] :as ctx} args value]
    (if-let [batch (:batch (:field-definition selection))]
      (let [batcher (init-batcher ctx batch args resolve-fn opts)
            result (resolve/resolve-promise)]
        (grouper/submit! batcher
                         value
                         :callback (partial resolve/deliver! result)
                         :errback (on-batch-error result batch))
        result)
      (first (resolve-fn ctx args (list value))))))


(defn batch-execute
  ([schema query args ctx] (batch-execute schema query args ctx nil))
  ([schema query args ctx options]
   (let [ctx' (merge {:batches (atom {})} ctx)
         result (graphql/execute schema query args ctx' options)]
     (future (run! grouper/shutdown! (vals @(:batches ctx'))))
     result)))


;; Sql Functions

(defn build-values-tuples
  [kws values]
  (->> values
       (map (apply juxt kws))
       (map-indexed #(conj %2 %1))
       (into [])))


(defn build-values-table
  [kws values]
  [[[:_values {:columns (conj kws :_index)}]
    {:values (build-values-tuples kws values)}]])


(defn build-batch-query
  [query kws values]
  (update query :with into (build-values-table kws values)))


(defn collect-results
  [values results]
  (let [step (fn [acc v] (update acc (:_index v) conj v))
        init (vec (repeat (count values) nil))]
    (reduce step init results)))


;; GraphQL Implementation

(defn people
  [ctx args values]
  (->> {:select [:people.*] :from [:people]}
       (sql/format)
       (jdbc/query (:db ctx))
       (list)))


(defn friends-query
  [ctx args values]
  (-> {:select [:people.* :_values._index]
       :from [:friends]
       :join [:_values [:= :friends.person_id :_values.id] :people
              [:= :people.id :friends.friend_id]]}
      (build-batch-query [:id] values)))


(defn friends
  [ctx args values]
  (->> (friends-query ctx args values)
       (sql/format)
       (jdbc/query db)
       (collect-results values)))


(def schema
  {:objects {:person {:fields {:id {:type 'Int}
                               :friends {:type '(list :person)
                                         :resolve ::friends
                                         :batch :batch/person}}}}
   ;;                           ^^^^^^ Adding/removing this key has a
   ;;                           significant
   ;;                                  impact on query performance!
   :queries {:people {:type '(list :person)
                      :args {:id {:type 'Int}}
                      :resolve ::people
                      :batch :batch/people}}})


(def resolvers
  {::people (with-batching people :interval 1 :debug true)
   ::friends (with-batching friends :interval 5 :debug true)})


(def compiled
  (-> schema
      (util/attach-resolvers resolvers)
      (schema/compile)))


;; Experiments

(def query-str
  "
  {
  , people {
  ,   id
  ,   friends {
  ,     id
  ,   }
  , }
  }
  ")


(defn execute-query [] (batch-execute compiled query-str nil {:db db}))


(defn timed-execute
  [& [show?]]
  (let [result (time (execute-query))] (when show? result)))

(defn reset-db
  []
  (jdbc/execute! db
                 [(str (slurp "resources/teardown.sql")
                       (slurp "resources/init.sql"))]))

#_(reset-db)

#_(clojure.pprint/pprint (timed-execute true))
