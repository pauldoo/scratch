; :mode=clojure:

; Fibonacci sequence in O(log(n)) iterations

(defn T [p q a b] [
    (+ (* b q) (* a q) (* a p))
    (+ (* b p) (* a q))])

(defn K [n p q a b]
    (if (= n 0) a
        (let [[a b] (if (even? n) [a b] (T p q a b))]
            (recur
                (quot n 2)
                (+ (* p p) (* q q))
                (+ (* 2 p q) (* q q))
                a b))))

(defn fib [n]
    (K (dec n) 0 1 1 0))


(fib 20)

