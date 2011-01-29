; :mode=clojure:

; Experiment to see if something like Mathematica's reap/sow can be done in Clojure
; http://reference.wolfram.com/mathematica/ref/Reap.html

(def reap-store)

(defn sow [v]
    "Adds the given value to the result that will be returned
    by the enclosing reap"
    (set! reap-store (conj reap-store v)))

(defn reap
    "Evaluates function 'f' ignoring the result.
    Calls to 'sow' by 'f' will store the sown value into
    the collection returned by reap."
    [f]
    (binding [reap-store []] (f) reap-store))

; Well that was easy..  now some tests..

(println
    (reap (fn [] (do
        (sow 1)
        (sow 2)
        (sow 3)))))

(defn sow-lots [v]
    (if (empty? v)
        nil
        (do
            (sow (first v))
            (recur (rest v)))))

(println
    (reap (fn [] (sow-lots [:a :b :c]))))

; Nested reaps

(println
    (reap (fn [] (do
        (sow 1)
        (sow 2)
        (sow (reap (fn [] (do
            (sow :a)
            (sow :b)))))))))

