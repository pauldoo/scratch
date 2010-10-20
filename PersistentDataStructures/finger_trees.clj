; :mode=clojure:

(defrecord node-2 [a b])
(defrecord node-3 [a b c])
(defrecord finger-tree-single [v])
(defrecord finger-tree-deep [l m r])

(defn make-node-3 [a b c] (node-3. a b c))

; Reverse cons
(defn snoc [a lst] (concat lst [a]))
; Reverse rest
(defn tser [lst] (take (dec (count lst)) lst))

; Adds a new element to the front of the finger tree
(defmulti push-front (fn [a _] (class a)))
(defmethod push-front nil [_ a]
    (finger-tree-single. a))
(defmethod push-front finger-tree-single [{r :v} a]
    (finger-tree-deep. [a] (delay nil) [r]))
(defmethod push-front finger-tree-deep [{l :l m :m r :r} a]
    (if (<= (count l) 3)
        (finger-tree-deep. (cons a l) m r)
        (finger-tree-deep.
            [a (first l)]
            (delay (push-front (force m) (apply make-node-3 (rest l))))
            r)))


; Adds a new element to the back of the finger tree
(defmulti push-back (fn [a _] (class a)))
(defmethod push-back nil [_ a]
    (finger-tree-single. a))
(defmethod push-back finger-tree-single [{r :v} a]
    (finger-tree-deep. [r] (delay nil) [a]))
(defmethod push-back finger-tree-deep [{l :l m :m r :r} a]
    (if (<= (count r) 3)
        (finger-tree-deep. l m (snoc a r))
        (finger-tree-deep.
            l
            (delay (push-back (force m) (apply make-node-3 (tser r))))
            [(last r) a])))

(defn go [t n func]
    (do (println t "\n")
        (if (<= n 0) nil (go (func t n) (dec n) func))))

(println "Forwards..")
(go nil 20 push-front)

(println "Backwards..")
(go nil 20 push-back)

