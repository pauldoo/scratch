; :mode=clojure:
(defproject spraff "1.0.0-SNAPSHOT"
    :description "IRC bot."
    :dependencies [
        [org.clojure/clojure "1.2.1"]
        [irclj "0.4.1-SNAPSHOT"]
        ]
    :dev-dependencies [
        [marginalia "0.5.1"]]
    :main spraff.core)

