; :mode=clojure:

(ns hello
    (:use
        [goog.dom :only [appendChild createDom]]
        [goog.events :only [listen]]
        [goog.events.EventType :only [CLICK]]
        ))

(def pos (atom 0))

(defn ^:export foobar []
    (let [canvas (goog.dom/getElement "my-canvas")
        ctx (.getContext canvas "2d")
        x (swap! pos inc)]
        (do
            (. ctx (save))
            (.clearRect ctx 0 0 (.width canvas) (.height canvas))
            (set! (.fillStyle ctx) "red")
            (.fillRect ctx x 100 100 50)
            (. ctx (restore))
            )))

(defn ^:export main []
    (let [body document.body ;(goog.dom/getElement "my-body")
        f (partial goog.dom/appendChild body)] (do
        (f
            (goog.dom/createDom "h1" {} "Hello!"))
        (f
            (goog.dom/createDom "canvas" (.strobj {
                "id" "my-canvas"
                "width" 600
                "height" 600
                "style" "border: 1px solid #000000"
                }) ""))
        (let [button (goog.dom/createDom "button" {} "Click!")] (do
            (goog.events/listen button goog.events.EventType/CLICK foobar)
            (f button)
            button))
)))

