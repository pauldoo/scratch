; :mode=clojure:

; Experiments with the y-combinator, initially copied from here:
; http://www.enrico-franchi.org/2011/01/y-combinator-in-clojure.html

(defn Y [F]
    (let [G (fn [K]
        (F (fn [& A]
            (apply (K K) A))))]
        (G G)))


(def fact
    (Y (fn [f]
        (fn [n]
            (if (<= n 0)
                1
                (* n (f (- n 1))))))))

(println (fact 5))

