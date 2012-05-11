; :mode=clojure:
(defproject spraff "1.0.0-SNAPSHOT"
    :description "IRC bot."
    :dependencies [
        [org.clojure/clojure "1.4.0"]
        [irclj "0.4.1-SNAPSHOT"]
        [ring/ring-jetty-adapter "1.1.0"]
        [ring/ring-core "1.1.0"]
        ]
    :main spraff.core)

