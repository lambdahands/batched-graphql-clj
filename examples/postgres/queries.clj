(ns postgres.queries
  (:require [batcher.helpers.postgres :as pg]
            [clojure.java.jdbc :as jdbc]
            [honeysql.core :as sql]))

(defn people-query [_ _ _]
  {:select [:people.*]
   :from   [:people]})

(defn people [ctx args values]
  (->> (people-query ctx args values)
       (sql/format)
       (jdbc/query (:db ctx))
       (list)))

(defn friends-query [ctx args values]
  (-> {:select [:people.* :_values._index]
       :from   [:friends]
       :join   [:_values [:= :friends.person_id :_values.id]
                :people [:= :people.id :friends.friend_id]]}
      (pg/build-batch-query [:id] values)))

(defn friends [ctx args values]
  (->> (friends-query ctx args values)
       (sql/format)
       (jdbc/query (:db ctx))
       (pg/collect-results values)))
