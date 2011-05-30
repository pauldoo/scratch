; :mode=clojure:

; Church numerals

(def zero (fn [f] (fn [x] x)))

; increment by one
(def add-1
    (fn [n] (fn [f] (fn [x] (f ((n f) x))))))

; equivalent to (add-1 zero)
(def one (fn [f] (fn [x] (f x))))

; equivalent to (add-1 (add-1 zero))
(def two (fn [f] (fn [x] (f (f x)))))

; direct addition, without use of add-1
(def add (fn [a b] (fn [f] (fn [x] ((a f) ((b f) x))))))

; decrement by one (not part of the exercise)
(def dec-1
    (fn [n] (fn [f] (fn [x] (((n (fn [g] (fn [h] (h (g f))))) (fn [u] x)) (fn [u] u))))))

; subtraction (not part of the exercise)
(def sub (fn [a b] ((b dec-1) a)))

; Convenience to convert to normal primitive integer
(defn to-int [n] ((n inc) 0))

; Some tests
(println
    (to-int (add-1 (add-1 (add-1 (add-1 zero))))) ; 4
    (to-int one) ; 1
    (to-int two) ; 2
    (to-int (dec-1 two)) ; 1
    (to-int (add one two)) ; 3
    (to-int (add (add-1 (add-1 (add-1 zero))) (add-1 (add-1 zero)))) ; 5
    (to-int (sub (add-1 (add-1 (add-1 (add-1 zero)))) (add-1 (add-1 zero)))) ; 2
)

