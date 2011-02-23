; :mode=clojure:

;; # Utilities #

(ns clojuroids.utilities)

(defn do-mod
    "Effectively calls (func coll v), taking care to wrap in a dosync and alter as 'coll' is assumed to be a ref."
    [coll func v]
    (dosync (alter coll (fn [x] (apply func [x v])))))

(defn now
    "Wall clock time in seconds relative to an arbitary epoch."
    [] (* 0.001 (System/currentTimeMillis)))

(defn myrand
    "Returns a random number in the range [min max), note inclusive exclusive."
    [min max] (+ min (rand (- max min))))



