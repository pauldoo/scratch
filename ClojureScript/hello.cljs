; :mode=clojure:

(ns hello
    (:use
        [goog.dom :only [appendChild createDom createTextNode removeChildren]]
        [goog.events :only [listen]]
        [goog.events.EventType :only [CLICK]]
        [goog.graphics :only [createGraphics Stroke SolidFill]]
        ))

(def pos (atom 0))
(def keys-pressed (atom (set)))
(def my-code-url "https://github.com/pauldoo/scratch/blob/master/ClojureScript/hello.cljs")

(defn print-to-log [v]
    (.log js/console v))

(defn foobar [t canvas-element y event] (do
    (print-to-log "hello")
    (cond
        (= t :up)
            (swap! keys-pressed disj (.keyCode event))
        (= t :down)
            (swap! keys-pressed conj (.keyCode event))
        :else
            nil)
    (let [x (swap! pos inc)
        graphics (goog.graphics/createGraphics 400 300)]
        (do
            (.drawCircle graphics x y 30 (Stroke. 2 "green") (SolidFill. "yellow"))
            (goog.dom/removeChildren canvas-element)
            (.render graphics canvas-element)
            ))))

(defn ^:export main []
    (let [
        canvas (goog.dom/createDom "div" {} "")
        button (goog.dom/createDom "button" {} "Click!")]
        (do
            (goog.events/listen button goog.events.EventType/CLICK (partial foobar :click canvas 100))
            (goog.events/listen document.body goog.events.EventType/KEYDOWN (partial foobar :down canvas 50) true)
            (goog.events/listen document.body goog.events.EventType/KEYUP (partial foobar :up canvas 150) true)
            (goog.dom/appendChild document.body (goog.dom/createDom "div" {}
                (goog.dom/createDom "h1" {} "Work in progress..")
                (goog.dom/createDom "div" {} "Try clicking the button, or pressing keys.")
                (goog.dom/createDom "div" {}
                    (goog.dom/createTextNode "My code lives here: ")
                    (goog.dom/createDom "a" (.strobj {"href" my-code-url}) my-code-url))
                button
                canvas)))))

