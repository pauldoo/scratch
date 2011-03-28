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

(defn wait-for-futures
    "Waits for the given collection of futures to all be completed. Enters a sleep-loop
    while it waits."
    [futures]
    (if (empty? futures)
        nil
        (do
            (while (not (future-done? (first futures)))
                (Thread/sleep 1))
            (recur (rest futures)))))

(defn drop-until
    "Drops elements from the front of a sequence until a predicate turns true.
    Returns the remainder of the sequence."
    [pred coll]
    (drop-while (complement pred) coll))

