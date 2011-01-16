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
(def fire-delay 0.3)
(def bullet-velocity 100)

(defn do-mod [coll func v]
    (dosync (alter coll (fn [x] (apply func [x v])))))

(defn player-shape []
    (doto
        (new Polygon)
        (.addPoint 10 0)
        (.addPoint -3 5)
        (.addPoint -3 -5)))

(defn bullet-shape []
    (doto
        (new Polygon)
        (.addPoint 2 0)
        (.addPoint -2 1)
        (.addPoint -2 -1)))

(defn draw-player [player g]
    (do
        (.setColor g (Color/WHITE))
        (let [old-transform (.getTransform g)]
            (.translate g (:x player) (:y player))
            (.rotate g (:a player))
            (.fill g (player-shape))
            (.setTransform g old-transform))))

(defn asteroid-shape [radii]
    ((fn [p r a ai]
        (if (empty? r)
            p
            (recur
                (doto p
                    (.addPoint
                        (* (Math/cos a) (first r))
                        (* (Math/sin a) (first r))))
                (rest r)
                (+ a ai)
                ai)))
        (new Polygon)
        radii
        0.0
        (/ (* Math/PI 2.0) (count radii))))

(defn draw-asteroid [asteroid g]
    (do
        (.setColor g (Color/GRAY))
        (let [old-transform (.getTransform g)]
            (.translate g (:x asteroid) (:y asteroid))
            (.rotate g (:a asteroid))
            (.fill g (asteroid-shape (:radii asteroid)))
            (.setTransform g old-transform))))

(defn draw-bullet [bullet g]
    (do
        (.setColor g (Color/YELLOW))
        (let [old-transform (.getTransform g)]
            (.translate g (:x bullet) (:y bullet))
            (.rotate g (:a bullet))
            (.fill g (bullet-shape))
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
        (draw-player (:player state) g))
        (doseq [a (:asteroids state)] (draw-asteroid a g))
        (doseq [b (:bullets state)] (draw-bullet b g)))

(defn create-component [game-state]
    (doto
        (proxy [JComponent] []
            (paint [g] (dosync (game-render (deref game-state) g))))
        (.setPreferredSize (new Dimension width height))
        (.setDoubleBuffered true)
        (.setFocusable true)))

(defn now [] (* 0.001 (System/currentTimeMillis)))

(defn myrand [min max]
    (+ min (rand (- max min))))

(defn step-thing [thing time-step eff]
    (assoc thing
        :x (mod (+ (:x thing) (* (:xv thing) time-step)) width)
        :y (mod (+ (:y thing) (* (:yv thing) time-step)) height)
        :a (mod (+ (:a thing) (* (:av thing) time-step)) (* Math/PI 2.0))
        :xv (* (:xv thing) eff)
        :yv (* (:yv thing) eff)
        :av (* (:av thing) eff)))

(defn rotate [x y a] [
    (+ (* (Math/cos a) x) (* (Math/sin a) y))
    (+ (* (- (Math/sin a)) x) (* (Math/cos a) y))])

(defn collided? [bullet asteroid]
    (let [
        [x y] (rotate
            (- (:x bullet) (:x asteroid))
            (- (:y bullet) (:y asteroid))
            (:a asteroid))
        p (asteroid-shape (:radii asteroid))]
        (.contains p x y)))

(defn filter-collisions [bullets asteroids]
    [
        (filter
            (fn [b] (nil? (some
                (fn [a] (collided? b a))
                asteroids)))
            bullets)
        (filter
            (fn [a] (nil? (some
                (fn [b] (collided? b a))
                bullets)))
            asteroids)])

(defn generate-asteroid []
    {
        :x (rand width)
        :y (rand height)
        :xv (rand 10)
        :yv (rand 10)
        :a 0.0
        :av (rand)
        :radii (take 10 (repeatedly #(myrand 5 30)))
    })

(defn default-state [] {
    :time (now)
    :time-step 0.0
    :player { :x 100.0 :y 100.0 :xv 0.0 :yv 0.0 :a 0.0 :av 0.0 }
    :next-fire-time (now)
    :asteroids (take 10 (repeatedly generate-asteroid))
} )

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
            (step-thing player time-step eff)
        ]
    player))

(defn asteroid-step [asteroid time-step]
    (step-thing asteroid time-step 1.0))

(defn new-bullet [player]
    (assoc player
        :xv (+ (:xv player) (* (Math/cos (:a player)) bullet-velocity))
        :yv (+ (:yv player) (* (Math/sin (:a player)) bullet-velocity))
))

(defn bullet-step [bullet time-step]
    (step-thing bullet time-step 1.0))

(defn game-step [state keys-pressed]
    (let [
        new-time (now)
        time-step (+ (* 0.95 (:time-step state)) (* 0.05 (- new-time (:time state))))
        spawn-new-bullet (and (contains? keys-pressed KeyEvent/VK_SPACE) (>= new-time (:next-fire-time state)))
        state
            (assoc state
                :time new-time
                :time-step time-step
                :player (player-step (:player state) time-step keys-pressed)
                :asteroids (map (fn [a] (asteroid-step a time-step)) (:asteroids state))
                :bullets
                    (map (fn [b] (bullet-step b time-step))
                        (concat
                            (if spawn-new-bullet
                                [(new-bullet (:player state))]
                                [])
                        (:bullets state)))
                :next-fire-time
                    (if spawn-new-bullet
                        (+ new-time fire-delay)
                        (:next-fire-time state)))
        [fb fa]
            (filter-collisions (:bullets state) (:asteroids state))
        state
            (assoc state
                :bullets fb
                :asteroids fa)]
    state))

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

