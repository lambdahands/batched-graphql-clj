(ns postgres.core
  (:require [batcher.core :as batcher :refer [with-batching]]
            [postgres.schema :as schema]
            [postgres.queries :as queries]
            [postgres.util :as util]
            [com.walmartlabs.lacinia.schema :as graphql-schema]
            [com.walmartlabs.lacinia.util :as graphql-util]))

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
