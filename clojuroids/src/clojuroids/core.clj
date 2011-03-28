; :mode=clojure:

;; ## Clojuroids ##
;; Asteroids written in Clojure.  I plan to experiment with the Clojure STM,
;; literate programming with Marginalia, and play with time (a la Braid).

;; # Todo list #
;; * Consider pure-ness of rendering functions, add ! if appropriate.
;; * Consider pure-ness of random number generation.. (hmm..)
;; * (This is affecting rewind playback in forward predictor thingie..)

(ns clojuroids.core
    (:gen-class)
    (:use
        [clojuroids [ui] [utilities]]
        [clojure.contrib.swing-utils :only [do-swing]]))

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
        [component & futures] (create-game)
        ]
        (do
            (.add (.getContentPane frame) component)
            (.addWindowListener frame (proxy [WindowAdapter] []
                (windowClosing [event]
                    (future
                        (dorun (map future-cancel futures))
                        (wait-for-futures futures)
                        (do-swing (.dispose frame))))
                (windowClosed [event]
                    (println "Closed.")
                    (System/exit 0))
            ))
            (.pack frame)
            (.setResizable frame false)
            (.show frame))))

