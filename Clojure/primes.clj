#!/usr/bin/env clj

; My first self-developed Clojure script!!
; (not just taken from an example in a book)

(defn isprime?
    ([x] (isprime? x 2))
    ([x n]
        (if (<= x n)
            true
            (if (zero? (rem x n))
                false
                (recur x (inc n))))))

(println
    (map
        (fn [k] (nth k 0))
        (filter
            (fn [k] (nth k 1))
            (map
                (fn [x] (vec [x (isprime? x)]))
                (take 42 (iterate inc 0))))))

