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
(def tick-step (/ 1 200))
(def render-step (/ 1 30))
(def gravity-strength 500)
(def ground-strength 200)
(def initial-game-state {
    :nodes {
        :t {
            :pos [150 100]
            :vel [0 0]
        }
        :u {
            :pos [350 200]
            :vel [0 0]
        }
        :v {
            :pos [250 100]
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
        {
            :node-a :u
            :node-b :v
            :rest-length 141.0
            :strength 2000
        }
        {
            :node-a :t
            :node-b :v
            :rest-length 100.0
            :strength 2000
        }
    ] })

(defn print-to-log [v]
    (.log js/console v))

(def vec- (partial map -))
(def vec+ (partial map +))
(defn vec* [v s] (map * v (repeat s)))
(def magnitude-squared (comp (partial apply +) (partial map #(* % %))))
(def magnitude (comp Math/sqrt magnitude-squared))
(def normalize (comp (partial apply vec*) (juxt identity (comp (partial / 1.0) magnitude))))

(defn calculate-force [node-a node-b rest-length strength]
    (let [a-b (vec- node-a node-b)
        pressure (* (- rest-length (magnitude a-b)) strength)
        force (vec* (normalize a-b) pressure)]
            force))

(defn calculate-spring-forces [nodes springs]
    (loop [forces {} springs springs]
        (if (empty? springs) forces
            (let [
                [s & r] springs
                node-a-id (s :node-a)
                node-b-id (s :node-b)
                force (calculate-force (:pos (nodes node-a-id)) (:pos (nodes node-b-id)) (:rest-length s) (:strength s))]
                (recur
                    (assoc forces
                        node-a-id (vec+ (get forces node-a-id [0 0]) force)
                        node-b-id (vec- (get forces node-b-id [0 0]) force))
                    r)))))

(defn calculate-gravity-forces [nodes]
    (zipmap (keys nodes) (repeat [0 (- gravity-strength)])))

(defn calculate-ground-forces [nodes]
    (zipmap (keys nodes)
        (map (fn [{[ x y ] :pos [ xv yv ] :vel }]
            (if (< y 0)
                (vec* [(- xv) (+ (Math/max 0 (- yv)) (- y))] ground-strength)
                [0 0])) (vals nodes))))

(defn calculate-all-node-forces [nodes springs]
    (merge-with vec+
        (calculate-spring-forces nodes springs)
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
                    (calculate-all-node-forces (:nodes state) (:springs state))
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

