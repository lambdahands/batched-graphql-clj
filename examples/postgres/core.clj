(ns postgres.core
  (:require [batcher.core :as batcher :refer [with-batching]]
            [postgres.schema :as schema]
            [postgres.queries :as queries]
            [postgres.util :as util]
            [com.walmartlabs.lacinia.schema :as graphql-schema]
            [com.walmartlabs.lacinia.util :as graphql-util]
            [org.bovinegenius.exploding-fish :as uri]))

(def resolvers
  {::schema/people  (with-batching queries/people :interval 1 :debug true)
   ::schema/friends (with-batching queries/friends :interval 5 :debug true)})

(def compiled-schema
  (-> schema/schema
      (graphql-util/attach-resolvers resolvers)
      (graphql-schema/compile)))

(def query-str "{ people { id friends { id } } }")

(defn execute-query
  [query db]
  (batcher/batch-execute compiled-schema query nil {:db db}))

(def default-db "postgresql://localhost:5432/graphql_batching")

(defn reset-example [db] (util/reset-db db))

(defn run-example [db] (execute-query query-str db))

(defn time-example [db] (time (run-example db)))

(defn with-env-db
  ([f] (with-env-db nil f))
  ([env-key f]
   (let [conn-str (or (System/getenv (or env-key "DATABASE_URL")) default-db)]
     (f (uri/param conn-str "loggerLevel" "OFF")))))

(defn try-reset
  []
  (try (with-env-db reset-example)
       (catch Exception e
         (println ">>> Could not reset database:")
         (println (.getMessage e))
         (System/exit 0))))

(defn env-time-example
  []
  (let [result (with-env-db time-example)]
    (if (:errors result)
      (do (println "Completed with errors:")
          (println (:errors result))
          (System/exit 1))
      (println :ok))))

(defn -main
  []
  (try-reset)
  (println :cold-run--start)
  (env-time-example)
  (println :warm-run--start)
  (env-time-example)
  (System/exit 0))
