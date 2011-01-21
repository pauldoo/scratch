; :mode=clojure:

; Asteroids written in Clojure

(import
    '(java.awt Color Dimension Polygon RenderingHints)
    '(javax.swing JComponent JFrame JLabel Timer)
    '(java.awt.event ActionListener KeyAdapter KeyEvent)
)

(def acceleration 100.0)
(def angular-acceleration 10.0)

(def player-efficiency 0.7)
(def bullet-efficiency 0.5)
(def bullet-acceleration 200)
(def sparkle-efficiency 0.3)
(def sparkle-velocity 0.25)
(def sparkle-amount 0.3)

(def width 640)
(def height 480)
(def fire-delay 0.3)

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
        (.addPoint 3 0)
        (.addPoint -3 1.5)
        (.addPoint -3 -1.5)))

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
            (.fill g (:poly asteroid))
            (.setTransform g old-transform))))

(defn draw-bullet [bullet g]
    (do
        (.setColor g (Color/YELLOW))
        (let [old-transform (.getTransform g)]
            (.translate g (:x bullet) (:y bullet))
            (.rotate g (:a bullet))
            (.fill g (bullet-shape))
            (.setTransform g old-transform))))

(defn draw-sparkle [sparkle g]
    (do
        (.setColor g (:sparkle-color sparkle))
        (let [old-transform (.getTransform g)]
            (.translate g (:x sparkle) (:y sparkle))
            (.fillRect g 0 0 1 1)
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
        (doseq [s (:sparkles state)] (draw-sparkle s g))
        (doseq [a (:asteroids state)] (draw-asteroid a g))
        (doseq [b (:bullets state)] (draw-bullet b g))
        (draw-player (:player state) g)
))

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

(defn step-thing [thing time-step]
    (let [eff (Math/pow (:eff thing) time-step)]
        (assoc thing
            :xv (+ (* (:xv thing) eff) (* time-step (:acc thing) (Math/cos (:a thing))))
            :yv (+ (* (:yv thing) eff) (* time-step (:acc thing) (Math/sin (:a thing))))
            :av (* (:av thing) eff)

            :x (mod (+ (:x thing) (* (:xv thing) time-step)) width)
            :y (mod (+ (:y thing) (* (:yv thing) time-step)) height)
            :a (mod (+ (:a thing) (* (:av thing) time-step)) (* Math/PI 2.0)))))

(defn rotate [x y a] [
    (+ (* (Math/cos a) x) (* (Math/sin a) y))
    (+ (* (- (Math/sin a)) x) (* (Math/cos a) y))])

(defn collided? [bullet asteroid]
    (let [
        [x y] (rotate
            (- (:x bullet) (:x asteroid))
            (- (:y bullet) (:y asteroid))
            (:a asteroid))
        p (:poly asteroid)]
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
    (let [a
        {
            :x (rand width)
            :y (rand height)
            :xv (rand 10)
            :yv (rand 10)
            :a 0.0
            :av (rand)
            :radii (take 10 (repeatedly #(myrand 5 30)))
            :eff 1.0
            :acc 0.0
        }]
    (assoc a
        :poly (asteroid-shape (:radii a)))))

(defn default-state [] {
    :time (now)
    :time-step 0.0
    :player {
        :x 100.0
        :y 100.0
        :xv 0.0
        :yv 0.0
        :a 0.0
        :av 0.0
        :acc 0.0
        :eff player-efficiency
        :sparkle-color Color/RED}
    :next-fire-time (now)
    :asteroids (take 10 (repeatedly generate-asteroid))
    :sparkles []
} )

(defn player-step [player time-step keys-pressed]
    (let [
        player
            (assoc player
                :acc (if (contains? keys-pressed KeyEvent/VK_UP) acceleration 0.0))
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

        player
            (step-thing player time-step)
        ]
    player))

(defn new-bullet [player]
    (assoc player
        :eff bullet-efficiency
        :acc bullet-acceleration
        :sparkle-color Color/PINK
))

(defn make-new-sparkles [objects time-step]
    (map
        (fn [o]
            (let [
                a (rand (* 2.0 Math/PI))
                v (rand sparkle-velocity)]
                (assoc o
                    :acc 0.0
                    :eff sparkle-efficiency
                    :xv (+ (:xv o) (* v (:acc o) (Math/cos a)))
                    :yv (+ (:yv o) (* v (:acc o) (Math/sin a))))))
        (filter
            (fn [o] (> (* (:acc o) time-step sparkle-amount) (rand)))
            objects)))

(defn mag [x y] (Math/sqrt (+ (* x x) (* y y))))

(defn sparkle-is-alive [sparkle]
    (> (mag (:xv sparkle) (:yv sparkle)) 2.0))

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
                :asteroids (doall (map (fn [a] (step-thing a time-step)) (:asteroids state)))
                :bullets
                    (doall (map (fn [b] (step-thing b time-step))
                        (concat
                            (if spawn-new-bullet
                                [(new-bullet (:player state))]
                                [])
                        (:bullets state))))
                :next-fire-time
                    (if spawn-new-bullet
                        (+ new-time fire-delay)
                        (:next-fire-time state))
                :sparkles
                    (doall (map (fn [s] (step-thing s time-step))
                        (concat
                            (make-new-sparkles (cons (:player state) (:bullets state)) time-step)
                            (filter sparkle-is-alive (:sparkles state))))))
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

            (future
                (try
                    (loop []
                        (do
                            (Thread/sleep 1)
                            (dosync (alter game-state (fn [state] (game-step state (deref keys-pressed)))))
                            (recur)))
                    (catch Exception e (.printStackTrace e))))

            result)))

(let [frame (new JFrame)]
    (do
        (.add (.getContentPane frame) (create-game))
        (.pack frame)
        (.setResizable frame false)
        (.show frame)))

