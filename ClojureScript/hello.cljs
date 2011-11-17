; :mode=clojure:

(ns hello
    (:use
        [goog.dom :only [appendChild createDom]]
        [goog.events :only [listen]]
        [goog.events.EventType :only [CLICK]]
        ))

(def pos (atom 0))

(defn foobar [canvas y]
    (let [ctx (.getContext canvas "2d")
        x (swap! pos inc)]
        (do
            (. ctx (save))
            (.clearRect ctx 0 0 (.width canvas) (.height canvas))
            (set! (.fillStyle ctx) "red")
            (.fillRect ctx x y 100 50)
            (. ctx (restore))
            )))

(defn ^:export main []
    (let [
        canvas (goog.dom/createDom "canvas" (.strobj {
            "width" 600
            "height" 600
            "style" "border: 1px solid #000000"
            }) "")
        button (goog.dom/createDom "button" {} "Click!")]
        (do
            (goog.events/listen button goog.events.EventType/CLICK (partial foobar canvas 100))
            (goog.events/listen document.body goog.events.EventType/KEYDOWN (partial foobar canvas 50) true)
            (goog.events/listen document.body goog.events.EventType/KEYUP (partial foobar canvas 150) true)
            (dorun (map (partial goog.dom/appendChild document.body)
                [(goog.dom/createDom "h1" {} "Hello!") canvas button])))))

