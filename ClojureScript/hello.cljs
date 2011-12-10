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
(def tick-step (/ 1 100))
(def render-step (/ 1 25))
(def gravity-strength 300)
(def ground-strength 100)
(def initial-game-state {
    :nodes {
        :t {
            :pos [50 100]
            :vel [0 0]
        }
        :u {
            :pos [250 200]
            :vel [0 0]
        }
        :v {
            :pos [150 100]
            :vel [0 0]
        }
    }
    :springs [
        {
            :node-a :t
            :node-b :u
            :rest-length 224.0
            :strength 2000
        }
        ;{
        ;    :node-a :u
        ;    :node-b :v
        ;    :rest-length 141.0
        ;    :strength 2000
        ;}
        {
            :node-a :t
            :node-b :v
            :rest-length 100.0
            :strength 2000
        }
    ]
    :hinges [
        {
            :node-a :u
            :node-b :t
            :node-c :v
            :rest-angle (/ Math/PI 2)
            :strength 100000
        }
    ]
    })

(defn print-to-log [v]
    (.log js/console v))

(def vec- (partial map -))
(def vec+ (partial map +))
(defn vec* [v s] (map * v (repeat s)))
(def magnitude-squared (comp (partial apply +) (partial map #(* % %))))
(def magnitude (comp Math/sqrt magnitude-squared))
(def normalize (comp (partial apply vec*) (juxt identity (comp (partial / 1.0) magnitude))))
(defn dot-product [v w] (apply + (map * v w)))
(defn clamp [v lower upper] (Math/min (Math/max v lower) upper))
(defn angle [v w] (Math/acos (clamp
    (dot-product (normalize v) (normalize w))
    -1.0 1.0 )))
(defn rot-cw [[x y]] [y (- x)])
(defn rot-ccw [[x y]] [(- y) x])

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

(defn calculate-hinge-force [a b c rest-angle strength]
    (let [
        b-to-a (vec- a b)
        b-to-c (vec- c b)
        pressure (* (- rest-angle (angle b-to-a b-to-c)) strength)
        result [
            (vec* (rot-ccw b-to-a) (/ pressure (magnitude-squared b-to-a)))
            (vec* (rot-cw b-to-c) (/ pressure (magnitude-squared b-to-c)))
        ]]
            result))

(defn calculate-hinge-forces [nodes hinges]
    (apply merge-with vec+
        (map
            (fn [{:keys [node-a node-b node-c rest-angle strength]}] (let [
                [force-a force-c] (calculate-hinge-force (:pos (nodes node-a)) (:pos (nodes node-b)) (:pos (nodes node-c)) rest-angle strength)]
                {
                    node-a force-a
                    node-b (vec- [0 0] force-a force-c)
                    node-c force-c
                }))
            hinges)))

(defn calculate-gravity-forces [nodes]
    (zipmap (keys nodes) (repeat [0 (- gravity-strength)])))

(defn calculate-ground-forces [nodes]
    (zipmap (keys nodes)
        (map (fn [{[ x y ] :pos [ xv yv ] :vel }]
            (if (< y 0)
                (vec* [(- xv) (+ (Math/max 0 (- yv)) (- y))] ground-strength)
                [0 0])) (vals nodes))))

(defn calculate-all-node-forces [{:keys [nodes springs hinges]}]
    (merge-with vec+
        (calculate-spring-forces nodes springs)
        (calculate-hinge-forces nodes hinges)
        (calculate-gravity-forces nodes)
        (calculate-ground-forces nodes)))

(defn update-node-velocities [nodes forces time-step]
    (merge-with (fn [n f]
        (assoc n
            :vel (vec+ (:vel n) (vec* f time-step))))
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

(defn tick-imp [state tick-step input-state]
    (assoc state
        :springs
            (let [modifier (cond
                    ((:keys input-state) KeyCodes/Q) 0.99
                    ((:keys input-state) KeyCodes/W) 1.01
                    :else 1.0)]
                (map (fn [s]
                    (assoc s :rest-length
                        (* modifier (:rest-length s))))
                        (:springs state)))
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
        graphics (graphics/createGraphics 400 300)
        {nodes :nodes springs :springs} game-state]
        (do
            (apply draw-line graphics (apply concat (map world-to-screen [[0 0] [400 0]])))
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
        button (dom/createDom "button" {} "Click!")
        tick-timer (goog.Timer. (Math/round (* 1000 tick-step)))
        render-timer (goog.Timer. (Math/round (* 1000 render-step)))
        ]
        (do
            (. tick-timer (start))
            (. render-timer (start))
            (dorun (map (partial apply events/listen) [
                [button EventType/CLICK (partial input-event input-state-ref)]
                [document.body EventType/KEYDOWN (partial input-event input-state-ref) true]
                [document.body EventType/KEYUP (partial input-event input-state-ref) true]
                [tick-timer Timer/TICK #(tick game-state-ref (deref input-state-ref))]
                [render-timer Timer/TICK #(rerender canvas (deref game-state-ref))]]))
            (dom/appendChild document.body (dom/createDom "div" {}
                (dom/createDom "h1" {} "Work in progress..")
                (dom/createDom "div" {} "Try clicking the button, or pressing keys.")
                (dom/createDom "div" {}
                    (dom/createTextNode "My code lives here: ")
                    (dom/createDom "a" (.strobj {"href" my-code-url}) my-code-url))
                button
                canvas)))))

