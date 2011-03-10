; :mode=clojure:

;; # Utilities #

(ns clojuroids.utilities)

(defn do-mod
    "Effectively calls (func coll v), taking care to wrap in a dosync and alter as 'coll' is assumed to be a ref."
    [coll func v]
    (dosync (alter coll (fn [x] (apply func [x v])))))

(def ^{:doc "Wall clock time in seconds relative to an arbitary epoch."}
    wall-time
    (let [offset (System/currentTimeMillis)]
        (fn [] (* 0.001 (- (System/currentTimeMillis) offset)))))

(defn myrand
    "Returns a random number in the range [min max), note inclusive exclusive."
    [min max] (+ min (rand (- max min))))


