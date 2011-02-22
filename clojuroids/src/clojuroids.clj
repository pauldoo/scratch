; :mode=clojure:

;; ## Clojuroids ##
;; Asteroids written in Clojure.  I plan to experiment with the Clojure STM,
;; literate programming with Marginalia, and play with time (a la Braid).

;; # Todo list #
;; * Split into multiple files
;; * Consider pure-ness of rendering functions, add ! if appropriate.
;; * Consider pure-ness of random number generation.. (hmm..)
;; * Consider pure-ness of game updates basing on wall clock time (should be passed from outside?)
;; * Manage lifetime of game update thread

(ns clojuroids
    (:gen-class)
    (:use [clojure.contrib.seq :only [separate]]))

(import
    '(java.awt Color Dimension Polygon RenderingHints)
    '(javax.swing JComponent JFrame JLabel Timer)
    '(java.awt.event ActionListener KeyAdapter KeyEvent)
)

;; # Constants #
;; Various constants used in the game.
;; *Linear Acceleration* values are in pixels per second per second.
;; *Angular Acceleration* values are in radians per second per second.
;; *Efficiency* values dictate the proportion of the object's velocity that remains after each passing second.

(def ^{:doc "The forward acceleration of the player when engines are firing."}
    acceleration 100.0)

(def ^{:doc "The angular acceleration of the player when they attempt to turn."}
    angular-acceleration 10.0)

(def ^{:doc "The player's efficiency value."}
    player-efficiency 0.7)

(def ^{:doc "The efficiency value for the bullets.  This is lower than the player's value to
    cause the bullets to straighten out fairly quickly"}
    bullet-efficiency 0.5)

(def ^{:doc "The forward acceleration of every bullet."}
    bullet-acceleration 200)

(def ^{:doc "The efficiency value for the sparkles that are given off by the player's engines
    and the bullets as they travel."}
    sparkle-efficiency 0.3)

(def ^{:doc "The velocity at which the sparkes spread in a random direction away from the
    emitting object.  This is a multiple of the emitting object's current acceleration."}
    sparkle-spread-velocity 0.25)

(def ^{:doc "The velocity at which the sparked kick backwards from the emitting object's
    current direction of acceleration.  This is a multiple of the emitting object's current acceleration."}
    sparkle-kick-velocity 1.0)

(def ^{:doc "Each accelerating object will emit roughly 'sparkle-amount * current-acceleration' sparkles per second."}
    sparkle-amount 0.3)

(def ^{:doc "Total number of sparkles permitted on the screen at once.  The limit is only
    present to keep performance good."}
    sparkle-limit 200)

(def ^{:doc "The radius of the smallest allowed asteroid."}
    smallest-asteroid 5.0)

(def ^{:doc "The radius of the staring asteroids."}
    initial-asteroid-size 30.0)

(def ^{:doc "Width of the playing field in pixels."}
    width 640)

(def ^{:doc "Height of the playing field in pixels."}
    height 480)

(def ^{:doc "The minimum delay (in seconds) between each bullet."}
    fire-delay 0.3)

;; # Utilities #

(defn do-mod
    "Effectively calls (func coll v), taking care to wrap in a dosync and alter as 'coll' is assumed to be a ref."
    [coll func v]
    (dosync (alter coll (fn [x] (apply func [x v])))))

(defn now
    "Wall clock time in seconds relative to an arbitary epoch."
    [] (* 0.001 (System/currentTimeMillis)))

(defn myrand
    "Returns a random number in the range [min max), note inclusive exclusive."
    [min max] (+ min (rand (- max min))))


;; # AWT/Swing graphics #

(defn player-shape
    "Creates an AWT polygon which is used as the shape of the player's ship on screen."
    []
    (doto
        (new Polygon)
        (.addPoint 10 0)
        (.addPoint -3 5)
        (.addPoint -3 -5)))

(defn bullet-shape
    "Creates an AWT polygon which is used as tht shape of each bullet on the screen."
    []
    (doto
        (new Polygon)
        (.addPoint 3 0)
        (.addPoint -3 1.5)
        (.addPoint -3 -1.5)))

(defn draw-player
    "Draws the player's ship to the given AWT Graphics2D object."
    [player g]
    (do
        (.setColor g (Color/WHITE))
        (let [old-transform (.getTransform g)]
            (.translate g (:x player) (:y player))
            (.rotate g (:a player))
            (.fill g (player-shape))
            (.setTransform g old-transform))))

(defn asteroid-shape
    "Constructs an AWT polygon from the polar radii of an asteroid."
    [radii]
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

(defn draw-asteroid
    "Draws an asteroid object to the given AWT Graphics2D object."
    [asteroid g]
    (do
        (.setColor g (Color/GRAY))
        (let [old-transform (.getTransform g)]
            (.translate g (:x asteroid) (:y asteroid))
            (.rotate g (:a asteroid))
            (.fill g (:poly asteroid))
            (.setTransform g old-transform))))

(defn draw-bullet
    "Draws a bullet object to the given AWT Graphics2D object."
    [bullet g]
    (do
        (.setColor g (Color/YELLOW))
        (let [old-transform (.getTransform g)]
            (.translate g (:x bullet) (:y bullet))
            (.rotate g (:a bullet))
            (.fill g (bullet-shape))
            (.setTransform g old-transform))))

(defn draw-sparkle
    "Draws a sparkle object to the given AWT Graphics2D object."
    [sparkle g]
    (do
        (.setColor g (:sparkle-color sparkle))
        (let [old-transform (.getTransform g)]
            (.translate g (:x sparkle) (:y sparkle))
            (.fillRect g 0 0 1 1)
            (.setTransform g old-transform))))

(defn game-render
    "Takes a game state consisting of the player ship, list of asteroids, list of bullets, and
    list of sparkles; and draws them all to the given AWT Graphics2D object."
    [state g]
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

;; # Game logic #

(defn step-thing
    "Updates an object (can be player, asteroid, or bullet) by the given time step (in seconds)
    according to it's current velocity and efficiency value.  Specialist behaviour for objects
    is applied elsewhere."
    [thing time-step]
    (let [eff (Math/pow (:eff thing) time-step)]
        (assoc thing
            :xv (+ (* (:xv thing) eff) (* time-step (:acc thing) (Math/cos (:a thing))))
            :yv (+ (* (:yv thing) eff) (* time-step (:acc thing) (Math/sin (:a thing))))
            :av (* (:av thing) eff)

            :x (mod (+ (:x thing) (* (:xv thing) time-step)) width)
            :y (mod (+ (:y thing) (* (:yv thing) time-step)) height)
            :a (mod (+ (:a thing) (* (:av thing) time-step)) (* Math/PI 2.0)))))

(defn rotate
    "Rotate a point (in 2D) around the origin by a given angle."
    [x y a]
    [(+ (* (Math/cos a) x) (* (Math/sin a) y))
    (+ (* (- (Math/sin a)) x) (* (Math/cos a) y))])

(defn collided?
    "Determines whether the given bullet and asteroid object have collided."
    [bullet asteroid]
    (let [
        ;; First transform the bullet into a frame of reference local to the asteroid.
        ;; This allows us to use the AWT Polgon's '.contains()' method.
        [x y] (rotate
            (- (:x bullet) (:x asteroid))
            (- (:y bullet) (:y asteroid))
            (:a asteroid))
        p (:poly asteroid)]
        (.contains p x y)))

(defn average
    "Given the polar radii for an asteroid, compute the average radius.  This is used
    when determining if a given asteroid is too small and should simply be annihilated."
    [radii]
    (/ (apply + radii) (count radii)))

(defn generate-asteroid
    "Generates a random asteroid from fresh."
    [x y radius]
    (let [a
        {
            :x x
            :y y
            :xv (myrand -20 20)
            :yv (myrand -20 20)
            :a 0.0
            :av (rand)
            :radii (take 10 (repeatedly #(myrand (* radius 0.5) (* radius 1.5))))
            :eff 1.0
            :acc 0.0
        }]
    (assoc a
        :poly (asteroid-shape (:radii a)))))

(defn split-asteroid
    "Splits the given asteroid into at most two pieces, typically as the result of it colliding with a bullet.
    May return fewer than two asteroids (even zero) if the chunks it splits into are deemed too small."
    [asteroid]
    (let [
        current-radius (average (:radii asteroid))
        a (rand (* Math/PI 2.0))
        xo (* current-radius (Math/cos a) 0.5)
        yo (* current-radius (Math/sin a) 0.5)
        ]
        (filter (fn [ast] (>= (average (:radii ast)) smallest-asteroid))
            [
                (generate-asteroid (+ (:x asteroid) xo) (+ (:y asteroid) yo) (* current-radius 0.6))
                (generate-asteroid (- (:x asteroid) xo) (- (:y asteroid) yo) (* current-radius 0.4))
            ])))

(defn filter-collisions
    "Given a list of bullets and asteroids, will detect collisions, split asteroids
    that need splitting, and remove the associated bullets."
    [bullets asteroids]
    [
        (filter
            (fn [b] (nil? (some
                (fn [a] (collided? b a))
                asteroids)))
            bullets)
        (let [
            [survived hit]
                (separate
                    (fn [a] (nil? (some
                        (fn [b] (collided? b a))
                        bullets)))
                    asteroids)]
            (apply concat (cons survived (map split-asteroid hit))))])

(defn default-state
    "Creates a fresh game state, which consists of the player, list of asteroids, timestamp, etc."
    []
    {
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
        :asteroids
            (take 10 (repeatedly #(generate-asteroid (rand width) (rand height) initial-asteroid-size)))
        :sparkles []
    } )

(defn player-step
    "Updates the player object by the given timestep, applying whatever movements are indicated by
    the currently pressed set of keyboard keys."
    [player time-step keys-pressed]
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

(defn new-bullet
    "Creates a new bullet object being released from the player's ship."
    [player]
    (assoc player
        :eff bullet-efficiency
        :acc bullet-acceleration
        :sparkle-color Color/PINK))

(defn make-new-sparkles
    "Creates new sparkle objects as needed for the list of objects given (typically
    the player ship plus list of bullets).  The sparkles kick backwards from the object
    as it accelerates with a random spread in direction."
    [objects time-step]
    ; Current implementation only allows one sparkle per object per update loop to be created.
    (map
        (fn [o]
            (let [
                a (rand (* 2.0 Math/PI))
                sv (* (rand sparkle-spread-velocity) (:acc o))
                v (* (rand sparkle-kick-velocity) (:acc o))]
                (assoc o
                    :acc 0.0
                    :eff sparkle-efficiency
                    :xv (+
                        (:xv o)
                        (* sv (Math/cos a))
                        (* v (- (Math/cos (:a o)))))
                    :yv (+
                        (:yv o)
                        (* sv (Math/sin a))
                        (* v (- (Math/sin (:a o))))))))
        (filter
            (fn [o] (> (* (:acc o) time-step sparkle-amount) (rand)))
            objects)))

(defn mag
    "Magnitude of a 2D vector (i.e. (x^2 + y^2)^0.5)."
    [x y] (Math/sqrt (+ (* x x) (* y y))))

(defn sparkle-is-alive
    "Determines if a sparkle is moving fast enough to warrant surviving to the next game state."
    [sparkle]
    (> (mag (:xv sparkle) (:yv sparkle)) 2.0))

(defn game-step
    "Updates the game state by the elapsed wallclock time since the previous update.  Takes
    into consideration any player actions that are represented by the set of currently held keyboard
    keys."
    [state keys-pressed]
    (let [
        ;; Should probably take the new time as a function argument, to be more pure.
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
                    (doall (take sparkle-limit
                        (map (fn [s] (step-thing s time-step))
                            (concat
                                (make-new-sparkles (cons (:player state) (:bullets state)) time-step)
                                (filter sparkle-is-alive (:sparkles state)))))))
        [fb fa]
            (filter-collisions (:bullets state) (:asteroids state))
        state
            (assoc state
                :bullets fb
                :asteroids fa)]
    state))

(defn create-game
    "Creates a Swing JComponent that represents the game window.  A swing timer
    is already attached to cause it to repaint at approximately 60Hz.  Also a background thread
    is created to update the game state at no faster than 1000Hz."
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

            (future
                ;; Need to consider the lifetime of this thread
                (try
                    (loop []
                        (do
                            (Thread/sleep 1)
                            (dosync (alter game-state (fn [state] (game-step state (deref keys-pressed)))))
                            (recur)))
                    (catch Exception e (.printStackTrace e))))

            result)))

(defn -main
    "Java entry point.  Creates a JFrame containing the appropriate Swing contents."
    [& args]
    (let [frame (new JFrame)]
        (do
            (.add (.getContentPane frame) (create-game))
            (.pack frame)
            (.setResizable frame false)
            (.show frame))))

