; :mode=clojure:

;; ## Clojuroids ##
;; Asteroids written in Clojure.  I plan to experiment with the Clojure STM,
;; literate programming with Marginalia, and play with time (a la Braid).

;; # Todo list #
;; * Consider pure-ness of rendering functions, add ! if appropriate.
;; * Consider pure-ness of random number generation.. (hmm..)
;; * Consider pure-ness of game updates basing on wall clock time (should be passed from outside?)

(ns clojuroids.core
    (:gen-class)
    (:use [clojuroids [ui]]))

(import
    '(java.awt.event WindowAdapter)
    '(javax.swing JFrame)
)

(defn -main
    "Java entry point.  Creates a JFrame containing the appropriate Swing contents.
    Also ensures that the game update thread is terminated when the window closes."
    [& args]
    (let [
        frame (new JFrame)
        [component update-thread] (create-game)
        ]
        (do
            (.add (.getContentPane frame) component)
            (.addWindowListener frame (proxy [WindowAdapter] []
                (windowClosing [event] (.dispose frame))
                (windowClosed [event] (future-cancel update-thread))))
            (.pack frame)
            (.setResizable frame false)
            (.show frame))))

