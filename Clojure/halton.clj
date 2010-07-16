#!/usr/bin/env clj

; Super lazy halton sequence of coordinates.
; The sequence of coordinates is lazy, and each individual
; coordinate has arbitary dimensions (evaluated lazily).

(load-file "primes.clj")

(defn halton-number
    "A single halton number"
    [n base]
    (if (zero? n)
        0
        (/
            (+
                (rem n base)
                (halton-number (quot n base) base))
            base)))

(defn lazy-halton-coordinate
    "A single halton coordinate, arbitary dimensions, evaluated lazily"
    [n]
    (map #(halton-number n %) (lazy-primes)))

(defn lazy-halton-coordinates
    "Super lazy halton sequence of coordinates."
    []
    (map #(lazy-halton-coordinate %) (iterate inc 0)))

; Now print the first 100 of the 8 dimensional halton coordinates
(println (take 100 (map #(take 8 %) (lazy-halton-coordinates))))

