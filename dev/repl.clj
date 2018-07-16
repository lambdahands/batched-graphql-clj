(ns repl
  (:require [cider-nrepl.main :as nrepl]
            [rebel-readline.main :as rebel]))

(defn -main
  []
  (nrepl/init ["cider.nrepl/cider-middleware"])
  (rebel/-main)
  (System/exit 0))
