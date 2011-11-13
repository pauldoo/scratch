; :mode=clojure:

(ns hello
    (:use [clojure.browser.dom :only [get-element]]))

(defn ^:export greet [n]
    (str "Hello " n))

(def pos (atom 0))

(defn ^:export foobar []
    (let [canvas (get-element "my-canvas")
        ctx (.getContext canvas "2d")
        x (swap! pos inc)]
        (do
            (. ctx (save))
            (.clearRect ctx 0 0 (.width canvas) (.height canvas))
            (set! (.fillStyle ctx) "red")
            (.fillRect ctx x 100 100 50)
            (. ctx (restore))
            )))

(defn ^:export test [] (foobar))

