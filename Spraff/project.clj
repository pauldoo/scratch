; :mode=clojure:
(defproject spraff "1.0.0-SNAPSHOT"
    :description "IRC bot."
    :dependencies [
        [org.clojure/clojure "1.4.0"]
        [irclj "0.4.1-SNAPSHOT"]
        ; NOTE: Actually needs a modified version of the tuples library - https://github.com/hiredman/tuples/pull/2
        [tuples "0.0.2"]
        ]
    :main spraff.core)

