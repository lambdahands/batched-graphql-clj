(ns postgres.util
  (:require [clojure.java.jdbc :as jdbc]))

(defn reset-sql
  []
  (str (slurp "resources/examples/teardown.sql")
       (slurp "resources/examples/init.sql")))

(defn reset-db [db] (jdbc/execute! db [(reset-sql)]))
