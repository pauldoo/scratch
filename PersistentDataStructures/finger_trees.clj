; :mode=clojure:

(defrecord node-2 [a b])
(defrecord node-3 [a b c])
(defrecord finger-tree-empty [])
(defrecord finger-tree-single [v])
(defrecord finger-tree-deep [l m r])

(defn make-node-3 [a b c] (node-3. a b c))

; Reverse cons
(defn snoc [a lst] (concat lst [a]))
; Reverse rest
(defn tser [lst] (take (dec (count lst)) lst))

; Adds a new element to the front of the finger tree
(defmulti push-front (fn [a _] (class a)))
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
(defmulti push-back (fn [a _] (class a)))
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


; Functions to print finger-trees to graphviz
(defn object-id [x] (System/identityHashCode x))

(defmulti print-entry (fn [entry parent-id] (class entry)))
(defmethod print-entry node-2 [node parent-id]
    (do
        (println (object-id node) " [label=\"Node2\"]")
        (println parent-id " -> " (object-id node))
        (print-entry (:a node) (object-id node))
        (print-entry (:b node) (object-id node))))
(defmethod print-entry node-3 [node parent-id]
    (do
        (println (object-id node) " [label=\"Node3\"]")
        (println parent-id " -> " (object-id node))
        (print-entry (:a node) (object-id node))
        (print-entry (:b node) (object-id node))
        (print-entry (:c node) (object-id node))))
(defmethod print-entry :default [entry parent-id]
    (do
        (println (object-id entry) " [label=\"" entry "\"]")
        (when parent-id (println parent-id " -> " (object-id entry)))))

(defn print-digits [node parent-id]
    (do
        (println (object-id node) " [label=\"Digits\"]")
        (when parent-id (println parent-id " -> " (object-id node)))
        (dorun (map (fn [x] (print-entry x (object-id node))) node))))

(defmulti print-finger-tree (fn [node parent-id] (class node)))
(defmethod print-finger-tree finger-tree-empty [_ _] nil)
(defmethod print-finger-tree finger-tree-single [node parent-id]
    (do
        (println (object-id node) " [label=\"Single\n" (:v node) "\"]")
        (when parent-id (println parent-id " -> " (object-id node)))))
(defmethod print-finger-tree finger-tree-deep [node parent-id]
    (do
        (println (object-id node) " [label=\"Deep\"]")
        (when parent-id (println parent-id " -> " (object-id node)))
        (print-digits (:l node) (object-id node))
        (print-finger-tree (force (:m node)) (object-id node))
        (print-digits (:r node) (object-id node))))

(defn print-finger-tree-to-file [tree filename]
    (spit filename (with-out-str (do
        (println "digraph {")
        (print-finger-tree tree nil)
        (println "}")))))

(defn go [t n func filename]
    (do
        (println t "\n")
        (if (<= n 0)
            (print-finger-tree-to-file t filename)
            (go (func t n) (dec n) func filename))))

(println "Forwards..")
(go (finger-tree-empty.) 20 push-front "/tmp/forwards.dot")

(println "Backwards..")
(go (finger-tree-empty.) 20 push-back "/tmp/backwards.dot")

