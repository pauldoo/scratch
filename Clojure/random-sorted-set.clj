; :mode=clojure:

; Hack to select random element from a Clojure sorted-set.
; The distribution will not be uniform, and will be something very uneven.

(defn my-comp [a b]
    (if (or (= :random a) (= :random b))
        (- (* 2 (rand-int 2)) 1)
        (compare a b)))

; Create test collection (of strings) using the special comparator
(def coll (apply sorted-set-by my-comp (map str (range 1 1000))))

(map #(first (subseq coll > %)) ["500" "200" "700" :random :random])
; ("501" "201" "701" "626" "286")

