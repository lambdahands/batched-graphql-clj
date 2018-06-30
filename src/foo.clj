(ns foo
  (:require [grouper.core :as grouper]
            [manifold.deferred :as d]
            [clojure.core.async :as a :refer [chan put!]]))


(defn db-select-singles
  [items]
  (let [latency-rtt 10  ; Network round-trip time between server and client
        latency-1   5] ; Time required for server to process a single query
    (for [id items]
      (do
       (Thread/sleep (+ latency-rtt latency-1))
       {:id id :seed (/ (* id id) Math/PI)}))))


(defn db-select-batch
  [items]
  (let [latency-rtt 10  ; Network round-trip time between server and client
        latency-1   5] ; Time required for server to process a single query
    (Thread/sleep (+ latency-rtt latency-1))
    (for [id items]
      {:id id :seed (/ (* id id) Math/PI)})))


(defn g-start []
  (grouper/start! (fn [items]
                    (println "Batching " (count items) " items")
                    (db-select-batch items))
                  :interval 1
                  :capacity 100))


(defn run-group [size mapping-fn]
  (let [g (g-start)]
     (map deref
          (doall
           (for [items (range size)]
             (d/chain (grouper/submit! g items) mapping-fn))))))


#_
(run-group 500 (fn [item]
                 (assoc item :uuid (java.util.UUID/randomUUID))))

(with-open [g (g-start)]
  (grouper/submit! g 1))

#_
(time (doall (db-select-singles (range 500))))
