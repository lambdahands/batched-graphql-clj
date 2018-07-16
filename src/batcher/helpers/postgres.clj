(ns batcher.helpers.postgres)

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
