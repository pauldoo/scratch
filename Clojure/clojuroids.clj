; :mode=clojure:

; Asteroids written in Clojure

(import
    '(java.awt Color Dimension Polygon RenderingHints)
    '(javax.swing JComponent JFrame JLabel Timer)
    '(java.awt.event ActionListener KeyAdapter KeyEvent)
)

(def acceleration 100.0)
(def efficiency 0.7)
(def angular-acceleration 10.0)
(def width 640)
(def height 480)

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
            (.rotate g (:a player))
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
        (.setPreferredSize (new Dimension width height))
        (.setDoubleBuffered true)
        (.setFocusable true)))

(defn now [] (* 0.001 (System/currentTimeMillis)))

(defn default-state [] {
    :time (now)
    :player { :x 100.0 :y 100.0 :xv 0.0 :yv 0.0 :a 0.0 :av 0.0 } } )



(defn player-step [player time-step keys-pressed]
    (let [
        player
            (if
                (contains? keys-pressed KeyEvent/VK_UP)
                (assoc player
                    :xv (+ (:xv player) (* time-step acceleration (Math/cos (:a player))))
                    :yv (+ (:yv player) (* time-step acceleration (Math/sin (:a player)))))
                player)
        player
            (if
                (contains? keys-pressed KeyEvent/VK_LEFT)
                (assoc player
                    :av (- (:av player) (* time-step angular-acceleration)))
                player)
        player
            (if
                (contains? keys-pressed KeyEvent/VK_RIGHT)
                (assoc player
                    :av (+ (:av player) (* time-step angular-acceleration)))
                player)
        eff
            (Math/pow efficiency time-step)

        player
            (assoc player
                :x (mod (+ (:x player) (* (:xv player) time-step)) width)
                :y (mod (+ (:y player) (* (:yv player) time-step)) height)
                :a (mod (+ (:a player) (* (:av player) time-step)) (* Math/PI 2.0))
                :xv (* (:xv player) eff)
                :yv (* (:yv player) eff)
                :av (* (:av player) eff))]

        player))


(defn game-step [state keys-pressed]
    (let [new-time (now) time-step (- new-time (:time state))]
        (assoc state
            :time new-time
            :player (player-step (:player state) time-step keys-pressed))))

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

