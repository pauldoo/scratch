; :mode=clojure:

;; ## Clojuroids ##
;; Asteroids written in Clojure.  I plan to experiment with the Clojure STM,
;; literate programming with Marginalia, and play with time (a la Braid).

;; # Todo list #
;; * Consider pure-ness of rendering functions, add ! if appropriate.
;; * Consider pure-ness of random number generation.. (hmm..)
;; * Consider pure-ness of game updates basing on wall clock time (should be passed from outside?)
;; * Manage lifetime of game update thread

(ns clojuroids.core
    (:gen-class)
    (:use [clojuroids [ui]]))

(import
    '(javax.swing JFrame)
)

(defn -main
    "Java entry point.  Creates a JFrame containing the appropriate Swing contents."
    [& args]
    (let [frame (new JFrame)]
        (do
            (.add (.getContentPane frame) (create-game))
            (.pack frame)
            (.setResizable frame false)
            (.show frame))))

