; :mode=clojure:
(defproject turbot "1.0.0-SNAPSHOT"
    :description "IRC bot."
    :dependencies [
        [org.clojure/clojure "1.4.0"]
        [irclj "0.4.1-SNAPSHOT"]
        [ring/ring-jetty-adapter "1.1.0"]
        [ring/ring-core "1.1.0"]
        [ring-json-response "0.2.0"]
        ]
    :main turbot.core)

