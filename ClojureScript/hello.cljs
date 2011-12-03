; :mode=clojure:

(ns hello
    (:use
        [goog.dom :only [appendChild createDom createTextNode removeChildren]]
        [goog.events :only [listen]]
        [goog.events.EventType :only [CLICK]]
        [goog.graphics :only [createGraphics Stroke SolidFill]]
        [goog :only [Timer]]
        ))

(def my-code-url "https://github.com/pauldoo/scratch/blob/master/ClojureScript/hello.cljs")

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

(defn calculate-node-forces [nodes springs]
    (loop [forces {} springs springs]
        (if (empty? springs) forces
            (let [
                [s & r] springs
                node-a-id (s :node-a)
                node-b-id (s :node-b)
                force (calculate-force (nodes node-a-id) (nodes node-b-id) (:rest-length s) (:strength s))]
                (recur
                    (assoc forces
                        node-a-id (vec+ (get forces node-a-id [0 0]) force)
                        node-b-id (vec- (get forces node-b-id [0 0]) force))
                    r)))))

(print-to-log
    (pr-str (calculate-node-forces {:t [1 1] :u [2 3] } [{:node-a :t :node-b :u :rest-length 2.0 :strength 5.0}] )))

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

(defn tick [game-state-ref input-state]
    (swap! game-state-ref
        (fn [{[x y] :pos}]
            {:pos [(inc x) (* 50 (count (:keys input-state)))]})))

(defn rerender [canvas-element game-state]
    (let [{[x y] :pos} game-state
        graphics (goog.graphics/createGraphics 400 300)]
        (do
            (.drawCircle graphics x y 30 (Stroke. 2 "green") (SolidFill. "yellow"))
            (goog.dom/removeChildren canvas-element)
            (.render graphics canvas-element)
            )))

(defn ^:export main []
    (let [
        game-state-ref (atom {:pos [0 0]})
        input-state-ref (atom {:keys (set)})
        canvas (goog.dom/createDom "div" {} "")
        button (goog.dom/createDom "button" {} "Click!")
        tick-timer (Timer. 100)
        render-timer (Timer. 100)
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

