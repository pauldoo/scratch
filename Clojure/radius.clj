; :mode=clojure:

(defn sin [v] (Math/sin v))
(defn cos [v] (Math/cos v))

(defn inner [n a b]
    (sin
        (if (zero? n)
            (- 1.0 (/ b a))
            (* (/ a b) (inner (dec n) a b))
        )
    )
)
(defn g [a b]
    (/ b (* 2 (inner 2000 a b))))

(g 3050.0 2985.0)
