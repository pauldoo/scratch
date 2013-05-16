; :mode=clojure:
(defproject spraff "1.0.0-SNAPSHOT"
    :description "IRC bot."
    :dependencies [
        [org.clojure/clojure "1.5.1"]
        [irclj "0.4.1-SNAPSHOT"]
        [tuples "0.0.2"] ; v0.0.3 uses significantly more memory due to cached hascode values :/
        ]
    :main spraff.core)

