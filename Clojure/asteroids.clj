; :mode=clojure:

(import
    '(java.awt Color Dimension Polygon RenderingHints)
    '(javax.swing JComponent JFrame JLabel Timer)
    '(java.awt.event ActionListener KeyAdapter)
)

(defn do-mod [coll func v]
    (dosync (alter coll (fn [x] (apply func [x v])))))

(defn player-shape []
    (doto
        (new Polygon)
        (.addPoint 10 0)
        (.addPoint -3 5)
        (.addPoint -3 -5)))

(defn draw-player [player g]
    (do
        (.setColor g (Color/WHITE))
        (let [old-transform (.getTransform g)]
            (.translate g (:x player) (:y player))
            (.fill g (player-shape))
            (.setTransform g old-transform))))

(defn game-render [state g]
    (do
        (.setRenderingHint g
            RenderingHints/KEY_ANTIALIASING
            RenderingHints/VALUE_ANTIALIAS_ON)
        (.setRenderingHint g
            RenderingHints/KEY_RENDERING
            RenderingHints/VALUE_RENDER_QUALITY)
        (.setColor g (Color/BLACK))
        (let [rect (.getClipBounds g)]
            (.fillRect g (.x rect) (.y rect) (.width rect) (.height rect)))
        (draw-player (:player state) g)))

(defn create-component [game-state]
    (doto
        (proxy [JComponent] []
            (paint [g] (dosync (game-render (deref game-state) g))))
        (.setPreferredSize (new Dimension 640 480))
        (.setDoubleBuffered true)
        (.setFocusable true)))

(defn now [] (* 0.001 (System/currentTimeMillis)))

(defn default-state [] {
    :time (now)
    :player { :x 100.0 :y 100.0 :xv 10.0 :yv 5.0 } } )

(defn player-step [player time-step]
    (assoc player
        :x (+ (:x player) (* (:xv player) time-step))
        :y (+ (:y player) (* (:yv player) time-step))))

(defn game-step [state keys-pressed]
    (let [new-time (now) time-step (- new-time (:time state))]
        (assoc state
            :time new-time
            :player (player-step (:player state) time-step))))

(defn create-game []
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

            (future (loop []
                (do
                    (Thread/sleep 1)
                    (dosync (alter game-state (fn [state] (game-step state (deref keys-pressed)))))
                    (recur))))

            result)))

(let [frame (new JFrame)]
    (do
        (.add (.getContentPane frame) (create-game))
        (.pack frame)
        (.setResizable frame false)
        (.show frame)))

