; :mode=clojure:

; 2-3 Finger Trees in Clojure.
; I have implemented only barebones finger trees and a counting monoid.
;
; I have not implemented other monoids/wrappers for sorted sets or priority queues
; but I have experimented on the REPL and am convinced they would work.
; This was my first piece of Clojure programming beyond "Hello World", so feedback
; is welcome!
;
; paul.richards@gmail.com
;

;(.printStackTrace *e)
;for i in /tmp/*.dot ; do echo $i && dot -Tsvg $i -o $i.svg; done

(defrecord monoid [measure-fn combine-fn])
(defrecord node-2 [v a b])
(defrecord node-3 [v a b c])
(defrecord finger-tree-empty [monoid])
(defrecord finger-tree-single [monoid e])
(defrecord finger-tree-deep [monoid v l m r])

(declare
    view-left
    view-right
    to-tree
    to-list)

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
        (if (or
                (and (not (empty? l)) (not (empty? r)))
                (instance? finger-tree-empty (force m)))
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
                (vec l) m (vec r))
            (throw (IllegalArgumentException. "middle tree is not empty, but a digit is")))
        (throw (IllegalArgumentException. "middle tree is not a delay"))))

; Reverse cons
(defn snoc [a lst] (concat lst [a]))
; Reverse rest
(defn tser [lst] (take (dec (count lst)) lst))


(defn deep-l [monoid l m r]
    (if (empty? l)
        (let [view (view-left (force m))]
            (if (nil? view)
                (to-tree monoid r)
                (let [[a mp] view]
                    (make-finger-tree-deep monoid (to-list a) mp r))))
        (make-finger-tree-deep monoid l m r)))

(defn deep-r [monoid l m r]
    (if (empty? r)
        (let [view (view-right (force m))]
            (if (nil? view)
                (to-tree monoid l)
                (let [[a mp] view]
                    (make-finger-tree-deep monoid l mp (to-list a)))))
        (make-finger-tree-deep monoid l m r)))

(defmulti view-left (fn [tree] (class tree)))
(defmethod view-left finger-tree-empty [_] nil)
(defmethod view-left finger-tree-single [{e :e monoid :monoid}]
    [e (delay (make-finger-tree-empty monoid))])
(defmethod view-left finger-tree-deep [{monoid :monoid l :l m :m r :r}]
    [(first l) (delay (deep-l monoid (rest l) m r))])

(defmulti view-right (fn [tree] (class tree)))
(defmethod view-right finger-tree-empty [_] nil)
(defmethod view-right finger-tree-single [{e :e monoid :monoid}]
    [e (delay (make-finger-tree-empty monoid))])
(defmethod view-right finger-tree-deep [{monoid :monoid l :l m :m r :r}]
    [(last r) (delay (deep-r monoid l m (tser r)))])

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

(defn force-early [d] (do (force d) d))

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
        (force-early (delay (app3
            (force (:m l))
            (apply nodes (:monoid l) (concat (:r l) m (:l r)))
            (force (:m r)))))
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
        (force-early (delay (app3 (force (:m a)) (apply nodes (:monoid a) (concat (:r a) (:l b))) (force (:m b)))))
        (:r b)))

(defmulti to-list (fn [tree] (class tree)))
(defmethod to-list node-2 [{a :a b :b}] [a b])
(defmethod to-list node-3 [{a :a b :b c :c}] [a b c])

(defn split-digit [p i [a & as] ems cfn]
    (if (nil? as)
        [[] a []]
        (let [ip (cfn i (ems a))]
            (if (p ip)
                [[] a as]
                (let [[l x r] (split-digit p ip as ems cfn)]
                    [(cons a l) x r])))))

(defmulti split-tree (fn [tree p i] (class tree)))
(defmethod split-tree finger-tree-single [{e :e m :monoid} _ _]
    [(make-finger-tree-empty m) e (make-finger-tree-empty m)])
(defmethod split-tree finger-tree-deep [{monoid :monoid l :l m :m r :r} p i]
    (let [cfn (:combine-fn monoid)
            ems (fn [v] (extract-measure v monoid))
            vl (cfn i (apply + (map ems l)))
            vm (cfn vl (ems (force m)))]
        (if (p vl)
            (let [[a b c] (split-digit p i l ems cfn)]
                [(to-tree monoid a) b (deep-l monoid c m r)])
            (if (p vm)
                (let [[ma mb mc] (split-tree (force m) p vl)
                        [a b c] (split-digit p (cfn vl (ems ma)) (to-list mb) ems cfn)]
                        [(deep-r monoid l (delay ma) a)
                        b
                        (deep-l monoid c (delay mc) r)])
                (let [[a b c] (split-digit p vm r ems cfn)]
                    [(deep-r monoid l m a) b (to-tree monoid c)])))))

(defmulti split (fn [tree p] (class tree)))
(defmethod split finger-tree-empty [tree _] [tree tree])
(defmethod split ::any [tree p]
    (let [monoid (:monoid tree)
        [l x r] (split-tree tree p ((:combine-fn monoid)))]
        (if (p (extract-measure tree monoid))
            [l (push-front r x)]
            [tree (make-finger-tree-empty monoid)])))

(defn take-until [tree p]
    (first (split tree p)))
(defn drop-until [tree p]
    (second (split tree p)))

(defn as-lazy-seq
    ([tree]
        (apply as-lazy-seq (view-left tree)))
    ([a next]
        (lazy-seq
            (cons a (apply as-lazy-seq (view-left (force next))))))
    ([] []))

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

(defn go [t vec func]
    (do
        (println t "\n")
        (if (empty? vec)
            t
            (go (func t (first vec)) (rest vec) func))))

(println "Forwards..")
(def t1 (go (make-finger-tree-empty count-monoid) (range 0 20) push-front))
(print-finger-tree-to-file t1 "/tmp/forwards.dot")

(println "Splitting")

(defn split-test [tree idx]
    (let [[l r] (split tree (fn [i] (< idx i)))]
        (do
            (print-finger-tree-to-file l (str "/tmp/forwards-t1-" (print-str idx) "-l.dot"))
            (print-finger-tree-to-file r (str "/tmp/forwards-t1-" (print-str idx) "-r.dot"))
            (println idx ": " (map as-lazy-seq [l r])))))

(doall (map (fn [x] (split-test t1 x)) (range -3 23)))

(println "Backwards..")
(def t2 (go (make-finger-tree-empty count-monoid) (range 20 40) push-back))
(print-finger-tree-to-file t2 "/tmp/backwards.dot")

(print-finger-tree-to-file
    (concatenate t1 t2)
    "/tmp/concat1.dot")

(print-finger-tree-to-file
    (concatenate t2 t1)
    "/tmp/concat2.dot")

