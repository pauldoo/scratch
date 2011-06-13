; :mode=clojure:

;; # Top level Swing UI #

(ns clojuroids.ui
    (:use
        [clojuroids [constants] [logic] [render] [utilities]]
        [clojure.contrib.swing-utils :only [do-swing do-swing-and-wait]]))

(import
    '(javax.swing JComponent JFrame SwingUtilities)
    '(java.awt.event ActionListener KeyAdapter KeyEvent WindowAdapter)
    '(java.awt Dimension)
)

(defn cons-game
    "Conses a gamestate to a list of gamespaces if the game-time is newer
    than the first state in the sequence."
    [state states]
    (if
        (or
            (empty? states)
            (> (:game-time state) (:game-time (first states))))
        (cons state states)
        states))

(defn create-component
    "Creates a Swing JComponent which will deref and draw the given game-state
    when painted.  It is currently the responsibility of the owning container to arrange
    for regular repaints."
    [game-state previous-states]
    (doto
        (proxy [JComponent] []
            (paint [g] (dosync (game-render (cons-game (deref game-state) (deref previous-states)) g))))
        (.setPreferredSize (new Dimension width height))
        (.setDoubleBuffered true)
        (.setFocusable true)))

(defn loopy-future
    "Creates a Clojure future that calls the supplied function 'func' in a loop.
    The function is initially supplied the given 'initial-args' as arguments, and on future
    iterations is given its previous output."
    [func & initial-args]
    (future (try
        (loop [args initial-args] (recur (apply func args)))
        (catch InterruptedException e
            (do-swing-and-wait (println "Loopy future canceled.")))
        (catch Exception e
            (do-swing-and-wait (.printStackTrace e))))))

(defn create-game
    "Creates a Swing JComponent that represents the game window.  A swing timer
    is already attached to cause it to repaint at approximately 60Hz.  Also returns a background
    thread which is created to update the game state at no faster than 1000Hz."
    [history create-frame-fn]
    (let [
        keys-pressed (ref (hash-set))
        game-state (ref (first history))
        previous-states (ref history)
        result (create-component game-state previous-states)
        ]
        (do
            (.addKeyListener result (proxy [KeyAdapter] []
                (keyPressed [event] (if (not (.isConsumed event)) (do
                    (do-mod keys-pressed conj (.getKeyCode event))
                    (.consume event))))
                (keyReleased [event] (if (not (.isConsumed event)) (do
                    (if (= (.getKeyCode event) KeyEvent/VK_F)
                        (create-frame-fn (deref previous-states)))
                    (do-mod keys-pressed disj (.getKeyCode event))
                    (.consume event))))))

            [
                result
                (loopy-future
                    (fn [old-wall-time time-step]
                        (Thread/sleep 1)
                        (dosync
                            (let
                                [[new-game-state new-previous-states]
                                (system-step
                                    (deref game-state)
                                    (deref previous-states)
                                    time-step
                                    (deref keys-pressed))]
                                (ref-set game-state new-game-state)
                                (ref-set previous-states new-previous-states)))
                        (let [new-wall-time (wall-time)] [
                            new-wall-time
                            (+  (* 0.95 time-step) (* 0.05 (- new-wall-time old-wall-time)))]))
                    (wall-time) 0.0)
                (loopy-future (fn []
                    (Thread/sleep (/ 1000 60))
                    (do-swing-and-wait (.repaint result))))
                (loopy-future (fn []
                    (Thread/sleep (/ 1000 20))
                    (dosync (ref-set previous-states (cons-game (deref game-state) (deref previous-states))))
                    nil))
            ])))

(defn create-frame
    "Creates a JFrame containing the appropriate Swing contents.
    Also ensures that the game update threads are terminated when the window closes."
    ([inc-fn dec-fn]
    (create-frame inc-fn dec-fn [(default-state)]))

    ([inc-fn dec-fn previous-states]
    (let [
        frame (new JFrame)
        [component & futures] (create-game
            previous-states (partial create-frame inc-fn dec-fn))
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
                (dec-fn))
        ))
        (.pack frame)
        (.setResizable frame false)
        (inc-fn)
        (.show frame)))))


