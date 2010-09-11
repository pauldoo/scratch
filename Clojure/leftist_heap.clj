(defstruct Tree :Rank :Element :Left :Right)

(defn create-empty [] nil)

(println
    (create-empty))

(defn rank [t]
    (if (nil? t)
        0
        (:Rank t)))

(defn make-t [element left right]
    (if (>= (rank left) (rank right))
        (struct Tree
            (inc (rank right))
            element
            left
            right)
        (struct Tree
            (inc (rank left))
            element
            right
            left)))

(defn merge-tree [t1 t2]
    (if (nil? t1)
        t2
        (if (nil? t2)
            t1
            (if (<= (:Element t1) (:Element t2))
                (make-t
                    (:Element t1)
                    (:Left t1)
                    (merge-tree
                        (:Right t1)
                        t2))
                (make-t
                    (:Element t2)
                    (:Left t2)
                    (merge-tree
                        t1
                        (:Right t2)))))))

(defn insert [element tree]
    (merge-tree
        (struct Tree 1 element nil nil)
        tree))

(defn peek-min [tree]
    (:Element tree))

(defn delete-min [tree]
    (merge-tree (:Left tree) (:Right tree)))

(defn pop-all [t]
    (if (nil? t)
        []
        (cons
            (peek-min t)
            (pop-all (delete-min t)))))

(def some-tree
    (insert 2
        (insert 10
            (insert 5
                (create-empty)))))

(println some-tree)

(println (peek-min some-tree))
(println (delete-min some-tree))
(println (merge-tree some-tree some-tree))
(println (pop-all some-tree))
(println (pop-all (merge-tree some-tree some-tree)))


