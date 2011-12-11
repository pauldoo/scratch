; :mode=clojure:

(ns hello
    (:require
        [goog.Timer :as Timer]
        [goog.dom :as dom]
        [goog.events :as events]
        [goog.events.EventType :as EventType]
        [goog.events.KeyCodes :as KeyCodes]
        [goog.graphics :as graphics]
        [goog.graphics.Stroke :as Stroke]
        [goog.graphics.SolidFill :as SolidFill]
        [goog.graphics.Path :as Path]
        ))

(def my-code-url "https://github.com/pauldoo/scratch/blob/master/ClojureScript/hello.cljs")
(def width-in-px 800)
(def height-in-px 300)
(def tick-step (/ 1 100))
(def render-step (/ 1 25))
(def gravity-strength 300)
(def ground-rebound-strength 200)
(def ground-grip-strength 50)
(def drag-factor 5)

(defn print-to-log [v]
    (.log js/console v))

(def vec- (partial map -))
(def vec+ (partial map +))
(defn vec* [v s] (map * v (repeat s)))
(def magnitude-squared (comp (partial apply +) (partial map #(* % %))))
(def magnitude (comp Math/sqrt magnitude-squared))
(def normalize (comp (partial apply vec*) (juxt identity (comp (partial / 1.0) magnitude))))
(defn dot-product [v w] (apply + (map * v w)))
(defn cross-product [[a b] [c d]] (- (* a d) (* b c)))
(defn clamp [v lower upper] (Math/min (Math/max v lower) upper))
(defn angle [v w] (Math/atan2 (cross-product v w) (dot-product v w)))
(defn rot-cw [[x y]] [y (- x)])
(defn rot-ccw [[x y]] [(- y) x])

(defn make-node [x y m] {:pos [x y] :vel [0 0] :mass m})
(defn make-spring [a b] {:node-a a :node-b b})
(defn make-hinge [a b c min-a max-a] {:node-a a :node-b b :node-c c :min-modifier-angle min-a :max-modifier-angle max-a})
(defn make-state [nodes springs hinges] {
    :nodes nodes
    :springs (map
        (fn [s] (assoc s
            :rest-length (magnitude (vec- (:pos (nodes (:node-a s))) (:pos (nodes (:node-b s)))))
            :strength 2000))
        springs)
    :hinges (map
        (fn [h] (assoc h
            :rest-angle (let [
                a (vec- (:pos (nodes (:node-a h))) (:pos (nodes (:node-b h))))
                c (vec- (:pos (nodes (:node-c h))) (:pos (nodes (:node-b h))))]
                (angle a c))
            :modifier-angle 0.0
            :strength 1000000))
        hinges)
})

(def initial-game-state (make-state
    {
        :hump (make-node 200 225 2)
        :head (make-node 300 150 2)
        :tail (make-node 100 150 2)

        :h-l-k (make-node 310 75 0.5)
        :h-l-f (make-node 300 0 0.5)
        :h-t-k (make-node 310 75 0.5)
        :h-t-f (make-node 300 0 0.5)

        :t-l-k (make-node 90 75 0.5)
        :t-l-f (make-node 100 0 0.5)
        :t-t-k (make-node 90 75 0.5)
        :t-t-f (make-node 100 0 0.5)
    }
    (map (partial apply make-spring) [
        [:hump :head]
        [:hump :tail]
        [:head :tail]
        [:head :h-l-k]
        [:h-l-k :h-l-f]
        [:head :h-t-k]
        [:h-t-k :h-t-f]
        [:tail :t-l-k]
        [:t-l-k :t-l-f]
        [:tail :t-t-k]
        [:t-t-k :t-t-f]
    ])
    (map (partial apply make-hinge) [
        [:tail :head :h-l-k -0.5 0.5]
        [:head :h-l-k :h-l-f -1 0.0]
        [:tail :head :h-t-k -0.5 0.5]
        [:head :h-t-k :h-t-f -1 0.0]
        [:head :tail :t-l-k -0.5 0.5]
        [:tail :t-l-k :t-l-f -1 0.0]
        [:head :tail :t-t-k -0.5 0.5]
        [:tail :t-t-k :t-t-f -1 0.0]
    ])
))

;(print-to-log (pr-str initial-game-state))

(defn calculate-force [node-a node-b rest-length strength]
    (let [a-b (vec- node-a node-b)
        pressure (* (- rest-length (magnitude a-b)) strength)
        force (vec* (normalize a-b) pressure)]
            force))

(defn calculate-spring-forces [nodes springs]
    (apply merge-with vec+
        (map
            (fn [{:keys [node-a node-b rest-length strength]}] (let [
                force (calculate-force (:pos (nodes node-a)) (:pos (nodes node-b)) rest-length strength)]
                {
                    node-a force
                    node-b (vec- force)
                }))
            springs)))

(defn real-mod [a b]
    (mod (+ (mod a b) b) b))
(defn pi-mod [a]
    (-
        (real-mod
            (+ a Math/PI)
            (* 2 Math/PI))
        Math/PI))

(defn calculate-hinge-force [a b c rest-angle strength]
    (let [
        b-to-a (vec- a b)
        b-to-c (vec- c b)
        pressure (* (pi-mod (- rest-angle (angle b-to-a b-to-c))) strength)
        result [
            (vec* (rot-cw b-to-a) (/ pressure (magnitude-squared b-to-a)))
            (vec* (rot-ccw b-to-c) (/ pressure (magnitude-squared b-to-c)))
        ]]
            result))

(defn calculate-hinge-forces [nodes hinges]
    (apply merge-with vec+
        (map
            (fn [{:keys [node-a node-b node-c rest-angle modifier-angle strength]}] (let [
                [force-a force-c] (calculate-hinge-force (:pos (nodes node-a)) (:pos (nodes node-b)) (:pos (nodes node-c)) (+ rest-angle modifier-angle) strength)]
                {
                    node-a force-a
                    node-b (vec- [0 0] force-a force-c)
                    node-c force-c
                }))
            hinges)))

(defn calculate-gravity-forces [nodes]
    (zipmap (keys nodes)
        (map (fn [{m :mass}]
            [0 (- (* gravity-strength m))])
            (vals nodes))))

(defn calculate-ground-forces [nodes]
    (zipmap (keys nodes)
        (map (fn [{[ x y ] :pos [ xv yv ] :vel }]
            (if (< y 0)
                (vec+
                    (vec* [(- xv) (Math/max 0 (- yv))] ground-grip-strength)
                    (vec* [0 (- y)] ground-rebound-strength))
                [0 0])) (vals nodes))))

(defn calculate-drag-forces [nodes]
    (zipmap (keys nodes)
        (map (fn [{v :vel m :mass}] (vec* v (- (* m drag-factor))))
            (vals nodes))))

(defn calculate-all-node-forces [{:keys [nodes springs hinges]}]
    (merge-with vec+
        (calculate-spring-forces nodes springs)
        (calculate-hinge-forces nodes hinges)
        (calculate-gravity-forces nodes)
        (calculate-ground-forces nodes)
        (calculate-drag-forces nodes)))

(defn update-node-velocities [nodes forces time-step]
    (merge-with (fn [n f]
        (assoc n
            :vel (vec+ (:vel n) (vec* f (/ time-step (:mass n))))))
        nodes
        forces))

(defn update-node-positions [nodes time-step]
    (zipmap (keys nodes) (map (fn [n]
        (assoc n
            :pos (vec+ (:pos n) (vec* (:vel n) time-step)))
        ) (vals nodes))))

(defn input-event [input-state-ref event]
    (swap! input-state-ref
        (fn [{keys :keys}]
            {:keys
                (cond
                    (= (.type event) EventType/KEYUP)
                        (disj keys (.keyCode event))
                    (= (.type event) EventType/KEYDOWN)
                        (conj keys (.keyCode event))
                    :else keys)})))

(defn hinge-nodes [h]
    (map h [:node-a :node-b :node-c]))

(defn tick-imp [state tick-step input-state]
    (assoc state
        :hinges
            (let
                [[fca fcb fka fkb rca rcb rka rkb] (map (:keys input-state) [
                    KeyCodes/I KeyCodes/O KeyCodes/K KeyCodes/L
                    KeyCodes/Q KeyCodes/W KeyCodes/A KeyCodes/S
                    ])]
                (map (fn [h] (let [hn (hinge-nodes h)]
                    (assoc h :modifier-angle
                        (clamp (+ (:modifier-angle h)
                            (cond
                                (= hn [:tail :head :h-l-k])
                                    (+ (if fca 0.05 0.0) (if fcb -0.05 0.0))
                                (= hn [:head :h-l-k :h-l-f])
                                    (+ (if fka 0.05 0.0) (if fkb -0.05 0.0))
                                (= hn [:tail :head :h-t-k])
                                    (+ (if fca -0.05 0.0) (if fcb 0.05 0.0))
                                (= hn [:head :h-t-k :h-t-f])
                                    (+ (if fka -0.05 0.0) (if fkb 0.05 0.0))
                                (= hn [:head :tail :t-l-k])
                                    (+ (if rca 0.05 0.0) (if rcb -0.05 0.0))
                                (= hn [:tail :t-l-k :t-l-f])
                                    (+ (if rka 0.05 0.0) (if rkb -0.05 0.0))
                                (= hn [:head :tail :t-t-k])
                                    (+ (if rca -0.05 0.0) (if rcb 0.05 0.0))
                                (= hn [:tail :t-t-k :t-t-f])
                                    (+ (if rka -0.05 0.0) (if rkb 0.05 0.0))
                                :else
                                    0.0))
                            (:min-modifier-angle h) (:max-modifier-angle h)))))
                    (:hinges state)))
        :nodes
            (update-node-positions
                (update-node-velocities
                    (:nodes state)
                    (calculate-all-node-forces state)
                    tick-step)
                tick-step)))

(defn tick [game-state-ref input-state] (do
    (swap! game-state-ref #(tick-imp % tick-step input-state))))

(defn world-to-screen [[x y]]
    [x (- 275 y)])

(defn draw-line [g x1 y1 x2 y2]
    (.drawPath g
        (let [p (goog.graphics.Path.)] (do
            (.moveTo p x1 y1)
            (.lineTo p x2 y2)
            p))
        (goog.graphics.Stroke. 1 "green") nil))

(defn rerender [canvas-element game-state]
    (let [
        graphics (graphics/createGraphics width-in-px height-in-px)
        {nodes :nodes springs :springs} game-state]
        (do
            (apply draw-line graphics (apply concat (map world-to-screen [[0 0] [width-in-px 0]])))
            (dorun (map
                (fn [{a :node-a b :node-b}]
                    (let [
                        [x1 y1] (world-to-screen (:pos (nodes a)))
                        [x2 y2] (world-to-screen (:pos (nodes b)))]
                        (draw-line graphics x1 y1 x2 y2))) springs))
            (dorun (map
                (fn [{p :pos}] (let [[x y] (world-to-screen p)]
                    (.drawCircle graphics x y 3 (goog.graphics.Stroke. 2 "green") (goog.graphics.SolidFill. "yellow"))))
                (vals nodes)))
            (dom/removeChildren canvas-element)
            (.render graphics canvas-element)
            )))

(defn ^:export main []
    (let [
        game-state-ref (atom initial-game-state)
        input-state-ref (atom {:keys (set)})
        canvas (dom/createDom "div" {} "")
        tick-timer (goog.Timer. (Math/round (* 1000 tick-step)))
        render-timer (goog.Timer. (Math/round (* 1000 render-step)))
        ]
        (do
            (. tick-timer (start))
            (. render-timer (start))
            (dorun (map (partial apply events/listen) [
                [document.body EventType/KEYDOWN (partial input-event input-state-ref) true]
                [document.body EventType/KEYUP (partial input-event input-state-ref) true]
                [tick-timer Timer/TICK #(tick game-state-ref (deref input-state-ref))]
                [render-timer Timer/TICK #(rerender canvas (deref game-state-ref))]]))
            (dom/appendChild document.body (dom/createDom "div" {}
                (dom/createDom "h1" {} "QWASIOKL!!")
                (dom/createDom "div" {} "Try pressing keys Q, W, A, S, I, O, K, and L.")
                (dom/createDom "div" {}
                    (dom/createTextNode "My code lives here: ")
                    (dom/createDom "a" (.strobj {"href" my-code-url}) my-code-url))
                button
                canvas)))))

