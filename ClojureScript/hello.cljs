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

