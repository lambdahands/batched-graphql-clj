(defproject grouper-test "0.0.1"
  :source-paths ["src/"]
  :dependencies [[org.clojure/clojure "1.9.0"] [junegunn/grouper "0.1.1"]
                 [manifold "0.1.8"] [com.walmartlabs/lacinia "0.28.0"]
                 [org.clojure/core.async "0.4.474"] [honeysql "0.9.2"]
                 [org.clojure/java.jdbc "0.7.7"]
                 [org.postgresql/postgresql "42.2.2"]]
  :plugins [[cider/cider-nrepl "0.17.0"]])
