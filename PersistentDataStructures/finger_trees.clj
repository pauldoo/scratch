; :mode=clojure:

(defrecord node-2 [a b])
(defrecord node-3 [a b c])
(defrecord finger-tree-single [v])
(defrecord finger-tree-deep [l m r])

; Adds a new element to the finger tree
(defmulti push-front (fn [a _] (class a)))
(defmethod push-front nil [_ a]
    (finger-tree-single. a))
(defmethod push-front finger-tree-single [{r :v} a]
    (finger-tree-deep. [a] nil [r]))
(defn make-node-3 [a b c] (node-3. a b c))
(defmethod push-front finger-tree-deep [{l :l m :m r :r} a]
    (if (<= (count l) 3)
        (finger-tree-deep. (cons a l) m r)
        (finger-tree-deep.
            [a (first l)]
            (push-front m (apply make-node-3 (rest l)))
            r))) ; not finished - needs to keep 'm'

(defn go [t n]
    (do (println t)
        (if (<= n 0) nil (go (push-front t n) (dec n)))))

(go nil 20)

