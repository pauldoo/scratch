; :mode=clojure:

(ns spraff.test.core
    (:gen-class)
    (:use
        [spraff.core]
        [clojure.test])
)

(deftest sentence-ends
    ;; Test that the Markov chain can terminate early if it wants
    (let [state (update-state "a b a b a b a b a b" empty-state)]
        ;; Should be able to create sentences of different lengths
        ;; Failure would be an infinite loop.
        (dorun
            (take 2 (distinct
                (map count (repeatedly #(generate-sentence state))))))))
