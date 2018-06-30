(defproject grouper-test "0.0.1"
  :source-paths ["src/"]
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [junegunn/grouper "0.1.1"]
                 [manifold "0.1.8"]
                 [com.walmartlabs/lacinia "0.28.0"]
                 [org.clojure/core.async "0.4.474"]]
  :plugins [[cider/cider-nrepl "0.17.0"]])
