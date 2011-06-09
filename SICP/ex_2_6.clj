; :mode=clojure:

; Church numerals, and prime number finder.
; Doesn't use any primitive data types (e.g. int or bool), only functions (and nil).
; Initially prompted by ex 2.6 from SICP.


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

; multiplication (not part of the exercise)
(def mul (fn [a b] (a b)))

; 'true'
(def true-val (fn [a _] a))

; 'false'
(def false-val (fn [_ b] b))

; 'if'
(def if-op (fn [p a b] ((p a b))))

; test for zero
(def is-zero? (fn [n] ((n (fn [_] false-val)) true-val)))

; test for equality
(def are-equal? (fn [a b]
    (if-op (is-zero? (sub a b))
        (fn [] (is-zero? (sub b a)))
        (fn [] false-val))))

; boolean negation
(def not-op (fn [p] (fn [a b] (p b a))))

; a < b
(def less-than? (fn [a b]
    (not-op (is-zero? (sub b a)))))

; a mod b
(def mod-op (fn [a b]
    (if-op (less-than? a b)
        (fn [] a)
        (fn [] (mod-op (sub a b) b)))))

(def is-prime-imp?
    (fn [n t]
        (if-op (less-than? t n)
            (fn []
                (if-op (is-zero? (mod-op n t))
                    (fn [] false-val)
                    (fn [] (is-prime-imp? n (add-1 t)))))
            (fn [] true-val))))

; Test for primality.
(def is-prime? (fn [n] (is-prime-imp? n two)))

; 'cons'
(def pair (fn [h t] (fn [p] (p h t))))

; 'first' (similar to true-val, but implementing independently for clarity)
(def first-op (fn [c] (c (fn [h _] h))))
; 'rest' (similar to false-val, but implementing independently for clarity)
(def rest-op (fn [c] (c (fn [_ t] t))))

; List of all primes less than or equal to 'n'.
(def all-primes-up-to (fn [n]
    (if-op (less-than? n two)
        (fn [] nil)
        (fn [] (
            (fn [r]
                (if-op (is-prime? n)
                    (fn [] (pair n r))
                    (fn [] r)))
            (all-primes-up-to (dec-1 n)))))))


; --------------------------------------

; Convenience to convert to normal clojure integer
(defn to-int [n] ((n inc) 0))

; Convenience to convert from normal clojure integer
(defn from-int [n] (nth (iterate add-1 zero) n))

; Convenience to convert to normal clojure boolean
(defn to-bool [p] (p true false))

; Convenience to convert to normal clojure list
(defn to-list [c]
    (if (nil? c) (list) (cons (first-op c) (to-list (rest-op c)))))

; Some tests
(dorun (map println [
    (to-bool true-val) ; true
    (to-bool false-val) ; false
    (to-bool (are-equal? zero zero)) ; true
    (to-bool (are-equal? zero one)) ; false
    (to-bool (are-equal? one zero)) ; false
    (to-bool (are-equal? two two)) ; true
    (to-bool (are-equal? two one)) ; false
    (to-bool (are-equal? one two)) ; false
    (to-bool (is-zero? zero)) ; true
    (to-bool (is-zero? one)) ; false
    (to-bool (is-zero? two)) ; false
    (to-bool (is-zero? (sub two two))) ; true
    (to-int (add-1 (add-1 (add-1 (add-1 zero))))) ; 4
    (to-int one) ; 1
    (to-int two) ; 2
    (to-int (dec-1 two)) ; 1
    (to-int (add one two)) ; 3
    (to-int (mul two two)) ; 4
    (to-int (mod-op (from-int 5) (from-int 3))) ; 2
    (to-int (add (from-int 3) (from-int 2))) ; 5
    (to-int (sub (from-int 4) (from-int 2))) ; 2
    (to-bool (is-prime? (from-int 2))) ; true
    (to-bool (is-prime? (from-int 3))) ; true
    (to-bool (is-prime? (from-int 4))) ; false
    (to-bool (is-prime? (from-int 5))) ; true
    (to-bool (is-prime? (from-int 6))) ; false
    (map to-int (to-list (all-primes-up-to (from-int 100)))) ; all primes <= 100
]))


