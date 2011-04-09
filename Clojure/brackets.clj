; :mode=clojure:

(defn bracket
    ([n]
        (bracket (* 2 n) 0))
    ([r h]
        (lazy-seq (if (zero? r)
            '("")
            (concat
                (if (> r h)
                    (map
                        (partial str "(")
                        (bracket (dec r) (inc h))))
                (if (> h 0)
                    (map
                        (partial str ")")
                        (bracket (dec r) (dec h)))))))))

(apply println (bracket 0))
(apply println (bracket 1))
(apply println (bracket 2))
(apply println (bracket 3))
(apply println (bracket 4))
(apply println (take 10 (bracket 100)))

