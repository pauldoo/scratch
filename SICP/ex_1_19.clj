; :mode=clojure:

; Fibonacci sequence in O(log(n)) iterations

(defn T [p q]
    (fn [a b] [
        (+ (* b q) (* a q) (* a p))
        (+ (* b p) (* a q))]))

(defn comb-fn [A B]
    (fn [a b] (apply A (B a b))))

(defn K [n p q]
    (if (= n 0)
        (fn [a b] [a b])
        (let
            [r (K (quot n 2)
                (+ (* p p) (* q q))
                (+ (* 2 p q) (* q q)))]
            (if (even? n)
                r
                (comb-fn (T p q) r)))))

(defn fib [n]
    (first ((K (dec n) 0 1) 1 0)))


(fib 20)

