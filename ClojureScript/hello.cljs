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
        [goog.date.UtcDateTime :as UtcDateTime]
        ))

(def my-code-url "https://github.com/pauldoo/scratch/blob/master/ClojureScript/hello.cljs")
(def width-in-px 800)
(def height-in-px 300)
(def ideal-tick-step (/ 1 100))
(def worst-tick-step (/ 1 50))
(def ideal-render-step (/ 1 25))
(def gravity-strength 500)
(def ground-rebound-strength 500)
(def ground-drag-factor 30)
(def air-drag-factor 1)

(defn print-to-log [v]
    (.log js/console v))

(defn print-passer [v] (do
    (print-to-log (pr-str v))
    v))


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
            :strength 500))
        springs)
    :hinges (map
        (fn [h] (assoc h
            :rest-angle (let [
                a (vec- (:pos (nodes (:node-a h))) (:pos (nodes (:node-b h))))
                c (vec- (:pos (nodes (:node-c h))) (:pos (nodes (:node-b h))))]
                (angle a c))
            :modifier-angle 0.0
            :strength 2000000))
        hinges)
})

(def initial-game-state (make-state
    {
        :head-a (make-node 270 200 1)
        :tail-a (make-node 130 200 1)
        :head (make-node 300 150 2)
        :tail (make-node 100 150 2)

        :h-l-k (make-node 300 75 0.5)
        :h-l-f (make-node 300 0 0.5)
        :h-t-k (make-node 300 75 0.5)
        :h-t-f (make-node 300 0 0.5)

        :t-l-k (make-node 100 75 0.5)
        :t-l-f (make-node 100 0 0.5)
        :t-t-k (make-node 100 75 0.5)
        :t-t-f (make-node 100 0 0.5)

        :top-of-head (make-node 320 200 1)
        :end-of-tail (make-node 80 200 1)
    }
    (map (partial apply make-spring) [
        [:head-a :head]
        [:tail-a :tail]
        [:head-a :tail-a]
        [:top-of-head :head]
        [:end-of-tail :tail]
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
        [:tail :head :h-l-k -0.7 0.7]
        [:head :h-l-k :h-l-f -1.2 0.2]
        [:tail :head :h-t-k -0.7 0.7]
        [:head :h-t-k :h-t-f -1.2 0.2]
        [:head :tail :t-l-k -0.7 0.7]
        [:tail :t-l-k :t-l-f -1.2 0.2]
        [:head :tail :t-t-k -0.7 0.7]
        [:tail :t-t-k :t-t-f -1.2 0.2]
        [:tail :head :top-of-head 0.0 0.0]
        [:head :tail :tail-a 0.0 0.0]
        [:tail :tail-a :head-a 0.0 0.0]
        [:tail-a :head-a :head 0.0 0.0]
        [:head-a :head :tail 0.0 0.0]
        [:head :tail :end-of-tail 0.0 0.0]
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
        (map (fn [{[ _ y ] :pos}]
            (if (< y 0)
                (vec* [0 (- y)] ground-rebound-strength)
                [0 0]))
            (vals nodes))))

(defn calculate-ground-impulses [nodes time-step]
    (let [loss (- 1.0 (Math/exp (- (* time-step ground-drag-factor))))]
        (zipmap (keys nodes)
            (map (fn [{[ _ y ] :pos [ xv yv ] :vel mass :mass}]
                (if (< y 0)
                    (vec* [(- xv) (Math/max 0 (- yv))] (* loss mass))
                    [0 0]))
                (vals nodes)))))

(defn calculate-air-impulses [nodes time-step]
    (let [loss (- 1.0 (Math/exp (- (* time-step air-drag-factor))))]
        (zipmap (keys nodes)
            (map (fn [{[_ y] :pos vel :vel mass :mass}]
                (if (< y 0)
                    [0 0]
                    (vec* vel (* -1 loss mass))))
                (vals nodes)))))

(defn calculate-all-node-forces [{:keys [nodes springs hinges]}]
    (merge-with vec+
        (calculate-spring-forces nodes springs)
        (calculate-hinge-forces nodes hinges)
        (calculate-gravity-forces nodes)
        (calculate-ground-forces nodes)))

(defn calculate-all-node-impulses [nodes time-step]
    (merge-with vec+
        (calculate-ground-impulses nodes time-step)
        (calculate-air-impulses nodes time-step)))

(defn update-node-velocities [nodes forces impulses time-step]
    (merge-with
        (fn [n i] (assoc n
            :vel (vec+
                (:vel n)
                (vec* i (/ 1.0 (:mass n))))))
        (merge-with
            (fn [n f] (assoc n
                :vel (vec+
                    (:vel n)
                    (vec* f (/ time-step (:mass n))))))
            nodes
            forces)
        impulses))

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

(defn now-in-milliseconds []
    (. (goog.date.UtcDateTime.) (getTime)))

(defn delta [f]
    (let [prev (atom (f))] (fn []
        (let [next (f) result (- next @prev)] (do
            (reset! prev next)
            result)))))

(defn limiter [f]
    (fn [] (Math/min (* 1000 worst-tick-step) (f))))

(defn smooth [f]
    (let [prev (atom 0.0)] (fn []
        (let [next (f) result (+ (* 0.01 next) (* 0.99 @prev))] (do
            (reset! prev result)
            result)))))

(def real-tick-step
    (smooth (limiter (delta now-in-milliseconds))))

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
                    (calculate-all-node-impulses (:nodes state) tick-step)
                    tick-step)
                tick-step)))

(defn tick [game-state-ref input-state] (let [real-step (* 0.001 (real-tick-step))] (do
    (swap! game-state-ref #(tick-imp % real-step input-state)))))

(defn world-to-screen [[x y]]
    [x (- 275 y)])

(defn draw-line [g x1 y1 x2 y2]
    (.drawPath g
        (let [p (goog.graphics.Path.)] (do
            (.moveTo p x1 y1)
            (.lineTo p x2 y2)
            p))
        (goog.graphics.Stroke. 3 "brown") nil))

(defn rerender [canvas-element game-state]
    (let [
        graphics (graphics/createGraphics width-in-px height-in-px)
        {:keys [nodes springs]} game-state]
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
                    (.drawCircle graphics x y 5 (goog.graphics.Stroke. 2 "brown") (goog.graphics.SolidFill. "brown"))))
                (vals nodes)))
            (let [head (.drawImage graphics -75 -75 150 150 "rudolf.png")
                [x y] (world-to-screen (:pos (:top-of-head nodes)))
                angle (* (/ 180 Math/PI) (angle (vec- (:pos (:top-of-head nodes)) (:pos (:head nodes))) [1 1]))
                ]
                (.setElementTransform graphics head x y angle 0 0))
            (dom/removeChildren canvas-element)
            (.render graphics canvas-element)
            )))

(defn ^:export main []
    (let [
        game-state-ref (atom initial-game-state)
        input-state-ref (atom {:keys (set)})
        canvas (dom/createDom "div" {} "")
        tick-timer (goog.Timer. (Math/round (* 1000 ideal-tick-step)))
        render-timer (goog.Timer. (Math/round (* 1000 ideal-render-step)))
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
                (dom/createDom "div" {}
                    (dom/createTextNode "This was my entry for the ")
                    (dom/createDom "a" (.strobj {"href" "http://groups.google.com/group/techmeetup/browse_thread/thread/e9462c7a7815aee"}) "2011 christmas competition")
                    (dom/createTextNode " at ")
                    (dom/createDom "a" (.strobj {"href" "http://techmeetup.co.uk"}) "TechMeetup")
                    (dom/createTextNode ".")
                )
                (dom/createDom "div" {}
                    (dom/createTextNode "Doesn't perform well in all browsers, so try Chrome if it seems sluggish."))
                canvas
                (dom/createDom "div" {}
                    (dom/createDom "a" (.strobj {"href" "http://pauldoo.com/"}) "http://pauldoo.com/"))
                )))))

