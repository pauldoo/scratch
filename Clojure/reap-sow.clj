; :mode=clojure:

; Experiment to see if something like Mathematica's reap/sow can be done in Clojure
; http://reference.wolfram.com/mathematica/ref/Reap.html

(def reap-store {})

(defn sow
    "Adds the given value to the result that will be returned
    by the enclosing (or tagged) reap"
    ([tag v] (let [r (reap-store tag)]
        (dosync (ref-set r (conj @r v)))))
    ([v] (sow :default-tag v)))

(defn reap
    "Evaluates function 'f' ignoring the result.
    Calls to 'sow' by 'f' will store the sown value into
    the collection returned by reap.

    Reaps are tagged, and for the result to appear in this
    reap the call to sow must use a matching tag.  This allows
    multiple reaps to overlap each other on the callstack without
    confusion."
    ([tag f] (let [r (ref [])]
        (binding [reap-store (assoc reap-store tag r)] (f))
        @r))
    ([f] (reap :default-tag f)))

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

; Tagged nested reaps

(println
    (reap :foo (fn [] (do
        (sow :foo 1)
        (sow :foo 2)
        (sow :foo
            (reap :bar (fn [] (do
                (sow :foo 3)
                (sow :bar :x)
                (sow :bar :y)
                (sow :bar :z)
                (sow :foo 4)))))
        (sow :foo 5)))))


