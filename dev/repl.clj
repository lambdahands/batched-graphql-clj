(ns repl
  (:require [cider-nrepl.main :as nrepl]
            [rebel-readline.main :as rebel]
            [clojure.edn :as edn]))

(defn -main
  [& args]
  (nrepl/start-nrepl {:middleware ["cider.nrepl/cider-middleware"]
                      :bind (System/getenv "NREPL_BIND")
                      :port (edn/read-string (System/getenv "NREPL_PORT"))})
  (rebel/-main)
  (System/exit 0))
