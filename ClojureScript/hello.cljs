; :mode=clojure:

(ns hello
    (:use
        [goog.dom :only [appendChild createDom createTextNode removeChildren]]
        [goog.events :only [listen]]
        [goog.events.EventType :only [CLICK]]
        [goog.graphics :only [createGraphics Stroke SolidFill Path]]
        [goog :only [Timer]]
        ))

(def my-code-url "https://github.com/pauldoo/scratch/blob/master/ClojureScript/hello.cljs")
(def tick-step (/ 1 100))
(def render-step (/ 1 30))
(def gravity-strength 15)
(def ground-strength 10)
(def initial-game-state {
    :nodes {
        :t {
            :pos [150 100]
            :vel [(- 10) 0]
        }
        :u {
            :pos [350 200]
            :vel [(- 10) 0]
        }
        :v {
            :pos [250 100]
            :vel [(- 10) 0]
        }
    }
    :springs [
        {
            :node-a :t
            :node-b :u
            :rest-length 224.0
            :strength 50
        }
        {
            :node-a :u
            :node-b :v
            :rest-length 141.0
            :strength 50
        }
        {
            :node-a :t
            :node-b :v
            :rest-length 100.0
            :strength 50
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
                (vec* [(- xv) (+ (- yv) (- y))] ground-strength)
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
                    (= (.type event) goog.events.EventType/KEYUP)
                        (disj keys (.keyCode event))
                    (= (.type event) goog.events.EventType/KEYDOWN)
                        (conj keys (.keyCode event))
                    :else keys)})))

(defn tick-imp [state tick-step]
    (assoc state :nodes
        (update-node-positions
            (update-node-velocities
                (:nodes state)
                (calculate-all-node-forces (:nodes state) (:springs state))
                tick-step)
            tick-step)))

(defn tick [game-state-ref input-state] (do
    (swap! game-state-ref #(tick-imp % tick-step))))

(defn world-to-screen [[x y]]
    [x (- 275 y)])

(defn draw-line [g x1 y1 x2 y2]
    (.drawPath g
        (let [p (Path.)] (do
            (.moveTo p x1 y1)
            (.lineTo p x2 y2)
            p))
        (Stroke. 1 "green") nil))

(defn rerender [canvas-element game-state]
    (let [
        graphics (goog.graphics/createGraphics 400 300)
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
                    (.drawCircle graphics x y 3 (Stroke. 2 "green") (SolidFill. "yellow"))))
                (vals nodes)))
            (goog.dom/removeChildren canvas-element)
            (.render graphics canvas-element)
            )))

(defn ^:export main []
    (let [
        game-state-ref (atom initial-game-state)
        input-state-ref (atom {:keys (set)})
        canvas (goog.dom/createDom "div" {} "")
        button (goog.dom/createDom "button" {} "Click!")
        tick-timer (Timer. (Math/round (* 1000 tick-step)))
        render-timer (Timer. (Math/round (* 1000 render-step)))
        ]
        (do
            (. tick-timer (start))
            (. render-timer (start))
            (goog.events/listen button goog.events.EventType/CLICK (partial input-event input-state-ref))
            (goog.events/listen document.body goog.events.EventType/KEYDOWN (partial input-event input-state-ref) true)
            (goog.events/listen document.body goog.events.EventType/KEYUP (partial input-event input-state-ref) true)
            (goog.events/listen tick-timer goog.Timer/TICK #(tick game-state-ref (deref input-state-ref)))
            (goog.events/listen render-timer goog.Timer/TICK #(rerender canvas (deref game-state-ref)))
            (goog.dom/appendChild document.body (goog.dom/createDom "div" {}
                (goog.dom/createDom "h1" {} "Work in progress..")
                (goog.dom/createDom "div" {} "Try clicking the button, or pressing keys.")
                (goog.dom/createDom "div" {}
                    (goog.dom/createTextNode "My code lives here: ")
                    (goog.dom/createDom "a" (.strobj {"href" my-code-url}) my-code-url))
                button
                canvas)))))

