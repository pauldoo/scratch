#!/usr/bin/env clj

; My first self-developed Clojure script!!
; (not just taken from an example in a book)

(defn isprime?
    ([x] (isprime? x 2))
    ([x n]
        (if (> (* n n) x)
            true
            (if (zero? (rem x n))
                false
                (recur x (inc n))))))

(defn lazy-primes
    []
    (filter
        isprime?
        (iterate inc 0)))

(println (take 42 (lazy-primes)))

