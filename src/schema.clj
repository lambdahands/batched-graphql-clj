(ns schema
  (:require [com.walmartlabs.lacinia.schema :as schema]
            [com.walmartlabs.lacinia.util :as util]
            [com.walmartlabs.lacinia.executor :as executor]
            [com.walmartlabs.lacinia :as graphql]
            [com.walmartlabs.lacinia.resolve :as resolve]
            [grouper.core :as grouper]
            [manifold.deferred :as d]))


;; Batching Library

(defn init-batcher [{:keys [batches] :as ctx} batch resolve-fn opts]
  (let [opts' (merge {:interval 1 :capacity 1000} opts)]
    (or (get @batches batch)
        (let [batcher (apply grouper/start!
                             (fn [items]
                               (println (format "Batching %d items in %s" (count items) batch))
                               (resolve-fn ctx items))
                             (apply concat opts'))]
          (swap! batches assoc batch batcher)
          batcher))))


(defn with-batching [resolve-fn & {:keys [interval max-capacity] :as opts}]
  (fn [{:keys [batches ::graphql/selection] :as ctx} args value]
    (if-let [batch (:batch (:field-definition selection))]
      (let [batcher (init-batcher ctx batch resolve-fn opts)
            result  (resolve/resolve-promise)]
        (grouper/submit! batcher {:args args :value value}
                         :callback #(resolve/deliver! result %))
        result)
      (first (resolve-fn ctx (list {:args args :value value}))))))


(defn batch-execute
  ([schema query args ctx]
   (batch-execute schema query args ctx nil))
  ([schema query args ctx options]
   (let [ctx' (merge {:batches (atom {})} ctx)
         result (graphql/execute schema query args ctx' options)]
     ; Cleanup groupers
     ;; (d/future (reduce-kv (fn [acc k v] (grouper/shutdown! v) acc)
     ;;                      {} @(:batches ctx)))
     result)))

;; GraphQL Implementation

(defn people [ctx items]
  (Thread/sleep 15)
  (map (fn [_] (repeatedly 30 #(hash-map :id (rand-int 1000)))) items))


(defn friends [ctx items]
  (Thread/sleep 5)
  (map (fn [_] (repeatedly 10 #(hash-map :id (rand-int 1000)))) items))

(def schema
  {:objects
   {:person {:fields {:id      {:type    'Int}
                      :friends {:type    '(list :person)
                                :resolve ::friends
                                :batch   :batch/person}}}}
   ;;                           ^^^^^^ Adding/removing this key has a significant
   ;;                                  impact on query performance!
   :queries
   {:people {:type    '(list :person)
             :args    {:id {:type 'Int}}
             :resolve ::people
             :batch   :batch/people}}})


(def resolvers
  {::people  (with-batching people)
   ::friends (with-batching friends)})


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


(defn timed-execute [& [show?]]
  (let [result (time (batch-execute compiled query-str nil {}))]
    (when show? result)))


#_
(timed-execute)
