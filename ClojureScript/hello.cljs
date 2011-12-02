; :mode=clojure:

(ns hello
    (:use
        [goog.dom :only [appendChild createDom createTextNode removeChildren]]
        [goog.events :only [listen]]
        [goog.events.EventType :only [CLICK]]
        [goog.graphics :only [createGraphics Stroke SolidFill]]
        [goog :only [Timer]]
        ))

; State (AAAAHAHAHG, don't touch it!)
(def pos (atom [0 100]))
(def keys-pressed (atom (set)))

(def my-code-url "https://github.com/pauldoo/scratch/blob/master/ClojureScript/hello.cljs")

(defn print-to-log [v]
    (.log js/console v))

(defn input-event [event]
    (cond
        (= (.type event) goog.events.EventType/KEYUP)
            (swap! keys-pressed disj (.keyCode event))
        (= (.type event) goog.events.EventType/KEYDOWN)
            (swap! keys-pressed conj (.keyCode event))
        :else
            nil))

(defn tick []
    (swap! pos (fn [[x y]] [(inc x) (* (count (deref keys-pressed)) 50)])))

(defn rerender [canvas-element]
    (let [[x y] (deref pos)
        graphics (goog.graphics/createGraphics 400 300)]
        (do
            (.drawCircle graphics x y 30 (Stroke. 2 "green") (SolidFill. "yellow"))
            (goog.dom/removeChildren canvas-element)
            (.render graphics canvas-element)
            )))

(defn ^:export main []
    (let [
        canvas (goog.dom/createDom "div" {} "")
        button (goog.dom/createDom "button" {} "Click!")
        tick-timer (Timer. 100)
        render-timer (Timer. 100)
        ]
        (do
            (. tick-timer (start))
            (. render-timer (start))
            (goog.events/listen button goog.events.EventType/CLICK input-event)
            (goog.events/listen document.body goog.events.EventType/KEYDOWN input-event true)
            (goog.events/listen document.body goog.events.EventType/KEYUP input-event true)
            (goog.events/listen tick-timer goog.Timer/TICK tick)
            (goog.events/listen render-timer goog.Timer/TICK #(rerender canvas))
            (goog.dom/appendChild document.body (goog.dom/createDom "div" {}
                (goog.dom/createDom "h1" {} "Work in progress..")
                (goog.dom/createDom "div" {} "Try clicking the button, or pressing keys.")
                (goog.dom/createDom "div" {}
                    (goog.dom/createTextNode "My code lives here: ")
                    (goog.dom/createDom "a" (.strobj {"href" my-code-url}) my-code-url))
                button
                canvas)))))

