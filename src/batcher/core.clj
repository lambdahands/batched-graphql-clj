(ns batcher.core
  (:require [com.walmartlabs.lacinia :as graphql]
            [com.walmartlabs.lacinia.resolve :as resolve]
            [grouper.core :as grouper]))

(defn build-resolver [ctx batch args resolve-fn opts]
  (fn [values]
    (when (:debug opts)
      (println (format "Batching %d values to %s" (count values) batch)))
    (resolve-fn ctx args values)))

(defn build-batcher-opts [{:keys [interval capacity] :or {interval 1 capacity 500}}]
  [:interval interval :capacity capacity])

(defn init-batcher [{:keys [batches] :as ctx} batch args resolve-fn opts]
  (let [batcher-opts (build-batcher-opts opts)]
    (or (get @batches batch)
        (let [resolver (build-resolver ctx batch args resolve-fn opts)
              batcher (apply grouper/start! resolver batcher-opts)]
          (swap! batches assoc batch batcher)
          batcher))))

(defn on-batch-error [result batch]
  (fn [error]
    (let [message (str "Exception: " (.getMessage error))]
      (resolve/deliver! result nil {:batch batch :message message}))))

(defn with-batching
  [resolve-fn & {:keys [interval max-capacity] :as opts}]
  (fn [{:keys [batches ::graphql/selection] :as ctx} args value]
    (if-let [batch (and batches (:batch (:field-definition selection)))]
      (let [batcher (init-batcher ctx batch args resolve-fn opts)
            result (resolve/resolve-promise)]
        (grouper/submit! batcher value
                         :callback (partial resolve/deliver! result)
                         :errback (on-batch-error result batch))
        result)
      (first (resolve-fn ctx args (list value))))))

(defn batch-execute
  ([schema query args ctx]
   (batch-execute schema query args ctx nil))
  ([schema query args ctx options]
   (let [ctx' (merge {:batches (atom {})} ctx)
         result (graphql/execute schema query args ctx' options)]
     (future (run! grouper/shutdown! (vals @(:batches ctx'))))
     result)))
