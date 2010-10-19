; :mode=clojure:

(defrecord node-2 [depth a b])
(defrecord node-3 [depth a b c])
(defrecord finger-tree-single [v])
(defrecord finger-tree-deep [l m r])

; Adds a new element to the finger tree
(defmulti push-front (fn [a _] (class a)))
(defmethod push-front nil [_ a]
    (finger-tree-single. a))
(defmethod push-front finger-tree-single [{r :v} a]
    (finger-tree-deep. [a] nil [r]))
(defn make-node-3 [d a b c] (node-3. d a b c))
(defmethod push-front finger-tree-deep [{l :l m :m r :r} a]
    (if (<= (count l) 3)
        (finger-tree-deep. (cons a l) m r)
        (finger-tree-deep. [a (first l)] (apply make-node-3 0 (rest l)) r))) ; not finished - needs to keep 'm'

(defn go [t n]
    (if (<= n 0) nil (do (println t) (go (push-front t n) (dec n)))))

(go nil 20)

