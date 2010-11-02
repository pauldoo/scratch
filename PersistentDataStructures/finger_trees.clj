; :mode=clojure:

(defrecord monoid [measure-fn combine-fn])
(defrecord node-2 [v a b])
(defrecord node-3 [v a b c])
(defrecord finger-tree-empty [monoid])
(defrecord finger-tree-single [monoid e])
(defrecord finger-tree-deep [monoid v l m r])

(derive Object ::any)

; Extract the (possibly cached) measure from an object.
; Some parts of the structure cache the monoid, which makes the argument redundant..
(defmulti extract-measure (fn [object monoid] (class object)))
(defmethod extract-measure node-2 [{v :v} _] v)
(defmethod extract-measure node-3 [{v :v} _] v)
(defmethod extract-measure finger-tree-empty [{{cf :combine-fn} :monoid} _] (cf))
(defmethod extract-measure finger-tree-single [{monoid :monoid e :e} _] (extract-measure e monoid))
(defmethod extract-measure finger-tree-deep [{v :v} _] (force v))
(defmethod extract-measure ::any [e {mf :measure-fn}] (mf e))

; Constructor functions
(defn make-node-2 [monoid a b]
    (node-2.
        (apply
            (:combine-fn monoid)
            (map (fn [e] (extract-measure e monoid)) [a b]))
        a b))
(defn make-node-3 [monoid a b c]
    (node-3.
        (apply
            (:combine-fn monoid)
            (map (fn [e] (extract-measure e monoid)) [a b c]))
        a b c))
(defn make-finger-tree-empty [monoid]
    (finger-tree-empty. monoid))
(defn make-finger-tree-single [monoid e]
    (finger-tree-single. monoid e))
(defn make-finger-tree-deep [monoid l m r]
    (if (delay? m)
        (finger-tree-deep.
            monoid
            (delay (apply
                (:combine-fn monoid)
                (map
                    (fn [e] (extract-measure e monoid))
                    (concat
                        l
                        ((fn [v] (if (nil? v) [] [v])) (force m))
                        r))))
            l m r)
        (throw (IllegalArgumentException. "middle tree is not a delay"))))

; Reverse cons
(defn snoc [a lst] (concat lst [a]))
; Reverse rest
(defn tser [lst] (take (dec (count lst)) lst))

; Adds a new element to the front of the finger tree
(defmulti push-front (fn [tree value] (class tree)))
(defmethod push-front finger-tree-empty [tree a]
    (make-finger-tree-single (:monoid tree) a))
(defmethod push-front finger-tree-single [{r :e monoid :monoid} a]
    (make-finger-tree-deep monoid [a] (delay (make-finger-tree-empty monoid)) [r]))
(defmethod push-front finger-tree-deep [{monoid :monoid l :l m :m r :r} a]
    (if (<= (count l) 3)
        (make-finger-tree-deep monoid (cons a l) m r)
        (make-finger-tree-deep
            monoid
            [a (first l)]
            (let [m-forced (force m)]
                (delay (push-front m-forced (apply make-node-3 monoid (rest l)))))
            r)))

; Adds a new element to the back of the finger tree
(defmulti push-back (fn [tree value] (class tree)))
(defmethod push-back finger-tree-empty [{monoid :monoid} a]
    (make-finger-tree-single monoid a))
(defmethod push-back finger-tree-single [{r :e monoid :monoid} a]
    (make-finger-tree-deep monoid [r] (delay (make-finger-tree-empty monoid)) [a]))
(defmethod push-back finger-tree-deep [{monoid :monoid l :l m :m r :r} a]
    (if (<= (count r) 3)
        (make-finger-tree-deep monoid l m (snoc a r))
        (make-finger-tree-deep
            monoid
            l
            (let [m-forced (force m)]
                (delay (push-back m-forced (apply make-node-3 monoid (tser r)))))
            [(last r) a])))

; Convenience function to convert a list into a finger tree
(defn to-tree [monoid vec] (
    (fn [tree vec] (if (empty? vec)
        tree
        (recur (push-back tree (first vec)) (rest vec))))
    (make-finger-tree-empty monoid)
    vec))

(defn nodes [monoid a b & r]
    (case (count r)
        0 [(make-node-2 monoid a b)]
        1 [(make-node-3 monoid a b (first r))]
        2 [(make-node-2 monoid a b) (apply make-node-2 monoid r)]
        (cons (make-node-3 monoid a b (first r)) (apply nodes monoid (rest r)))))

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
    (push-front (app3 (make-finger-tree-empty (:monoid l)) m r) (:e l)))
(defmethod app3 [::any finger-tree-single] [l m r]
    (push-back (app3 l m (make-finger-tree-empty (:monoid r))) (:e r)))
(defmethod app3 [finger-tree-deep finger-tree-deep] [l m r]
    (make-finger-tree-deep
        (:monoid l)
        (:l l)
        (app3
            (force (:m l))
            (apply nodes (:monoid l) (concat (:r l) m (:l r)))
            (force (:m r)))
        (:r r)))

(prefer-method app3 [finger-tree-empty ::any] [::any finger-tree-empty])
(prefer-method app3 [finger-tree-single ::any] [::any finger-tree-single])
(prefer-method app3 [finger-tree-empty ::any] [::any finger-tree-single])

(defmulti concatenate (fn [a b] [(class a) (class b)]))
(defmethod concatenate [::any finger-tree-empty] [a _] a)
(defmethod concatenate [finger-tree-empty ::any] [_ b] b)
(defmethod concatenate [::any finger-tree-single] [a b]
    (push-back a (:e b)))
(defmethod concatenate [finger-tree-single ::any] [a b]
    (push-front (:e a) b))
(defmethod concatenate [finger-tree-deep finger-tree-deep] [a b]
    (make-finger-tree-deep
        (:monoid a)
        (:l a)
        (app3 (force (:m a)) (apply nodes (:monoid a) (concat (:r a) (:l b))) (force (:m b)))
        (:r b)))

(defmulti split-tree (fn [tree p i] (class tree)))
(defmethod split-tree finger-tree-single [{e :e m :monoid} _ _]
    [(make-finger-tree-empty m) e (make-finger-tree-empty m)])
(defmethod split-tree finger-tree-deep [{monoid :monoid l :l m :m r :r} p i]
    (let [cfn (:combine-fn monoid)
            ems (fn [v] (extract-measure v monoid))
            vl (cfn i (ems l))
            vm (cfn vl (ems m))]
        (if (p vl)
            (let [[a b c] (split-digit p i l)]
                [(to-tree a) b (make-finger-tree-deep monoid c m r)])
            (if (p vm)
                (let [[ma mb mc] (split-tree p vl m)
                        [a b c] (split-digit p (cfn vl (ems ma)) (to-list mb))]
                    [(make-finger-tree-deep monoid l ma a) b (make-finger-tree-deep c mc)])
                (let [[a b c] (split-digit p vm r)]
                    [(make-finger-tree-deep monoid l m a) b (to-tree c)])))))

; Functions to print finger-trees to graphviz
(defn object-id [x] (System/identityHashCode x))

(defmulti print-entry (fn [entry parent-id] (class entry)))
(defmethod print-entry node-2 [node parent-id]
    (do
        (println (object-id node) " [label=\"Node2\\n" (extract-measure node nil) "\"]")
        (println parent-id " -> " (object-id node) " [weight=2]")
        (print-entry (:a node) (object-id node))
        (print-entry (:b node) (object-id node))))
(defmethod print-entry node-3 [node parent-id]
    (do
        (println (object-id node) " [label=\"Node3\\n" (extract-measure node nil) "\"]")
        (println parent-id " -> " (object-id node) " [weight=2]")
        (print-entry (:a node) (object-id node))
        (print-entry (:b node) (object-id node))
        (print-entry (:c node) (object-id node))))
(defmethod print-entry ::any [entry parent-id]
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
        (println (object-id node) " [label=\"Single\\n" (extract-measure node {:monoid node}) "\"]")
        (when parent-id (println parent-id " -> " (object-id node) " [weight=4]"))
        (print-entry (:e node) (object-id node))))
(defmethod print-finger-tree finger-tree-deep [node parent-id]
    (do
        (println (object-id node) " [label=\"Deep\\n" (extract-measure node {:monoid node}) "\"]")
        (when parent-id (println parent-id " -> " (object-id node) " [weight=4]"))
        (print-digits (:l node) (object-id node))
        (print-finger-tree (force (:m node)) (object-id node))
        (print-digits (:r node) (object-id node))))

(defn print-finger-tree-to-file [tree filename]
    (spit filename (with-out-str (do
        (println "digraph {")
        (print-finger-tree tree nil)
        (println "}")))))

(def count-monoid
    (monoid. (fn [_] 1) +))

(defn go [t vec func filename]
    (do
        (println t "\n")
        (if (empty? vec)
            (do
                (print-finger-tree-to-file t filename)
                t)
            (go (func t (first vec)) (rest vec) func filename))))

(println "Forwards..")
(def t1 (go (make-finger-tree-empty count-monoid) (range 1 20) push-front "/tmp/forwards.dot"))

(println "Backwards..")
(def t2 (go (make-finger-tree-empty count-monoid) (range 20 40) push-back "/tmp/backwards.dot"))

(print-finger-tree-to-file
    (concatenate t1 t2)
    "/tmp/concat1.dot")
(print-finger-tree-to-file
    (concatenate t2 t1)
    "/tmp/concat2.dot")

