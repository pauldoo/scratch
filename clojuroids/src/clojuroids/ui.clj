; :mode=clojure:

;; # Top level Swing UI #

(ns clojuroids.ui
    (:use [clojuroids [constants] [logic] [render] [utilities]]))

(import
    '(javax.swing JComponent Timer)
    '(java.awt.event ActionListener KeyAdapter KeyEvent)
    '(java.awt Dimension)
)

(defn create-component
    "Creates a Swing JComponent which will deref and draw the given game-state
    when painted.  It is currently the responsibility of the owning container to arrange
    for regular repaints."
    [game-state]
    (doto
        (proxy [JComponent] []
            (paint [g] (dosync (game-render (deref game-state) g))))
        (.setPreferredSize (new Dimension width height))
        (.setDoubleBuffered true)
        (.setFocusable true)))

(defn create-game
    "Creates a Swing JComponent that represents the game window.  A swing timer
    is already attached to cause it to repaint at approximately 60Hz.  Also returns a background
    thread which is created to update the game state at no faster than 1000Hz."
    []
    (let [
        keys-pressed (ref (hash-set))
        game-state (ref (default-state))
        result (create-component game-state)
        ]
        (do
            (doto
                result
                (.addKeyListener (proxy [KeyAdapter] []
                    (keyPressed [event] (if (not (.isConsumed event)) (do
                        (do-mod keys-pressed conj (.getKeyCode event))
                        (.consume event))))
                    (keyReleased [event] (if (not (.isConsumed event)) (do
                        (do-mod keys-pressed disj (.getKeyCode event))
                        (.consume event)))))))

            (doto
                (new Timer (/ 1000 60)
                    (reify ActionListener
                        (actionPerformed [this event] (.repaint result))))
                (.start))

            [result
            (future
                ;; Need to consider the lifetime of this thread
                (try
                    (loop [
                        old-wall-time (wall-time)
                        time-step 0.0]
                        (do
                            (Thread/sleep 1)
                            (dosync (alter game-state (fn [state] (game-step state time-step (deref keys-pressed)))))
                            (let [new-wall-time (wall-time)]
                                (recur
                                    new-wall-time
                                    (+  (* 0.95 time-step) (* 0.05 (- new-wall-time old-wall-time)))))))
                    (catch InterruptedException e (println "Stopping update thread."))
                    (catch Exception e (.printStackTrace e))))
            ])))


