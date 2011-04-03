; :mode=clojure:

;; ## Clojuroids ##
;; Asteroids written in Clojure.  I plan to experiment with the Clojure STM,
;; literate programming with Marginalia, and play with time (a la Braid).

;; # Todo list #
;; * Consider pure-ness of rendering functions, add ! if appropriate.
;; * Consider pure-ness of random number generation.. (hmm..)
;; * Some tidy up - especially to do with sparkles.

(ns clojuroids.core
    (:gen-class)
    (:use
        [clojuroids [ui] [utilities]]))

(import
    '(java.awt.event WindowAdapter)
    '(javax.swing JFrame)
)

(defn -main
    "Java entry point.  Counts number of visible frames (via callbacks) and
    terminates the application when none are visible."
    [& args]
    (let [
        counter (ref 0)
        inc-fn (fn []
            (dosync (alter counter inc)))
        dec-fn (fn []
            (if (= 0 (dosync (alter counter dec)))
            (System/exit 0)))]
    (create-frame inc-fn dec-fn)))

