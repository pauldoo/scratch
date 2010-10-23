; :mode=clojure:

(defrecord node-2 [a b])
(defrecord node-3 [a b c])
(defrecord finger-tree-empty [])
(defrecord finger-tree-single [v])
(defrecord finger-tree-deep [l m r])

(defn make-node-2 [a b] (node-2. a b))
(defn make-node-3 [a b c] (node-3. a b c))
(derive Object ::any)

; Reverse cons
(defn snoc [a lst] (concat lst [a]))
; Reverse rest
(defn tser [lst] (take (dec (count lst)) lst))

; Adds a new element to the front of the finger tree
(defmulti push-front (fn [tree value] (class tree)))
(defmethod push-front finger-tree-empty [_ a]
    (finger-tree-single. a))
(defmethod push-front finger-tree-single [{r :v} a]
    (finger-tree-deep. [a] (delay (finger-tree-empty.)) [r]))
(defmethod push-front finger-tree-deep [{l :l m :m r :r} a]
    (if (<= (count l) 3)
        (finger-tree-deep. (cons a l) m r)
        (finger-tree-deep.
            [a (first l)]
            (let [m-forced (force m)]
                (delay (push-front m-forced (apply make-node-3 (rest l)))))
            r)))

; Adds a new element to the back of the finger tree
(defmulti push-back (fn [tree value] (class tree)))
(defmethod push-back finger-tree-empty [_ a]
    (finger-tree-single. a))
(defmethod push-back finger-tree-single [{r :v} a]
    (finger-tree-deep. [r] (delay (finger-tree-empty.)) [a]))
(defmethod push-back finger-tree-deep [{l :l m :m r :r} a]
    (if (<= (count r) 3)
        (finger-tree-deep. l m (snoc a r))
        (finger-tree-deep.
            l
            (let [m-forced (force m)]
                (delay (push-back m-forced (apply make-node-3 (tser r)))))
            [(last r) a])))

(defn nodes [a b & r]
    (case (count r)
        0 [(node-2. a b)]
        1 [(node-3. a b (first r))]
        2 [(node-2. a b) (apply make-node-2 r)]
        (cons (node-3. a b (first r)) (apply nodes (rest r)))))

(defmulti app3 (fn [l m r] [(class l) (class r)]))
(defmethod app3 [finger-tree-empty ::any] [l m r]
    (if (empty? m)
        r
        (app3
            l
            (tser m)
            (push-front r (last m)))))
(defmethod app3 [::any finger-tree-empty] [l m r]
    (if (empty? m)
        l
        (app3
            (push-back l (first m))
            (rest m)
            r)))
(defmethod app3 [finger-tree-single ::any] [l m r]
    (push-front (app3 (finger-tree-empty.) m r) (:v l)))
(defmethod app3 [::any finger-tree-single] [l m r]
    (push-back (app3 l m (finger-tree-empty.)) (:v r)))
(defmethod app3 [finger-tree-deep finger-tree-deep] [l m r]
    (finger-tree-deep.
        (:l l)
        (app3
            (force (:m l))
            (apply nodes (concat (:r l) m (:l r)))
            (force (:m r)))
        (:r r)))

(prefer-method app3 [finger-tree-empty ::any] [::any finger-tree-empty])
(prefer-method app3 [finger-tree-single ::any] [::any finger-tree-single])
(prefer-method app3 [finger-tree-empty ::any] [::any finger-tree-single])

(defmulti concatenate (fn [a b] [(class a) (class b)]))
(defmethod concatenate [::any finger-tree-empty] [a _] a)
(defmethod concatenate [finger-tree-empty ::any] [_ b] b)
(defmethod concatenate [::any finger-tree-single] [a b]
    (push-back a (:v b)))
(defmethod concatenate [finger-tree-single ::any] [a b]
    (push-front (:v a) b))
(defmethod concatenate [finger-tree-deep finger-tree-deep] [a b]
    (finger-tree-deep.
        (:l a)
        (app3 (force (:m a)) (apply nodes (concat (:r a) (:l b))) (force (:m b)))
        (:r b)))

; Functions to print finger-trees to graphviz
(defn object-id [x] (System/identityHashCode x))

(defmulti print-entry (fn [entry parent-id] (class entry)))
(defmethod print-entry node-2 [node parent-id]
    (do
        (println (object-id node) " [label=\"Node2\"]")
        (println parent-id " -> " (object-id node) " [weight=2]")
        (print-entry (:a node) (object-id node))
        (print-entry (:b node) (object-id node))))
(defmethod print-entry node-3 [node parent-id]
    (do
        (println (object-id node) " [label=\"Node3\"]")
        (println parent-id " -> " (object-id node) " [weight=2]")
        (print-entry (:a node) (object-id node))
        (print-entry (:b node) (object-id node))
        (print-entry (:c node) (object-id node))))
(defmethod print-entry :default [entry parent-id]
    (do
        (println (object-id entry) " [label=\"" entry "\"]")
        (when parent-id (println parent-id " -> " (object-id entry) " [weight=1]"))))

(defn print-digits [node parent-id]
    (do
        (println (object-id node) " [label=\"Digits\"]")
        (when parent-id (println parent-id " -> " (object-id node) " [weight=3]"))
        (dorun (map (fn [x] (print-entry x (object-id node))) node))))

(defmulti print-finger-tree (fn [node parent-id] (class node)))
(defmethod print-finger-tree finger-tree-empty [_ _] nil)
(defmethod print-finger-tree finger-tree-single [node parent-id]
    (do
        (println (object-id node) " [label=\"Single\"]")
        (when parent-id (println parent-id " -> " (object-id node) " [weight=4]"))
        (print-entry (:v node) (object-id node))))
(defmethod print-finger-tree finger-tree-deep [node parent-id]
    (do
        (println (object-id node) " [label=\"Deep\"]")
        (when parent-id (println parent-id " -> " (object-id node) " [weight=4]"))
        (print-digits (:l node) (object-id node))
        (print-finger-tree (force (:m node)) (object-id node))
        (print-digits (:r node) (object-id node))))

(defn print-finger-tree-to-file [tree filename]
    (spit filename (with-out-str (do
        (println "digraph {")
        (print-finger-tree tree nil)
        (println "}")))))

(defn go [t vec func filename]
    (do
        (println t "\n")
        (if (empty? vec)
            (do
                (print-finger-tree-to-file t filename)
                t)
            (go (func t (first vec)) (rest vec) func filename))))

(println "Forwards..")
(def t1 (go (finger-tree-empty.) (range 1 20) push-front "/tmp/forwards.dot"))

(println "Backwards..")
(def t2 (go (finger-tree-empty.) (range 20 40) push-back "/tmp/backwards.dot"))

(print-finger-tree-to-file
    (concatenate t1 t2)
    "/tmp/concat1.dot")
(print-finger-tree-to-file
    (concatenate t2 t1)
    "/tmp/concat2.dot")

