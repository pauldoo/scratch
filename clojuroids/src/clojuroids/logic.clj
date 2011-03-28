; :mode=clojure:

;; # Game logic #

(ns clojuroids.logic
    (:use
        [clojuroids [constants] [render] [utilities]]
        [clojure.contrib.seq :only [separate]]))

(import
    '(java.awt Color Dimension Polygon RenderingHints)
    '(java.awt.event ActionListener KeyAdapter KeyEvent)
    '(javax.swing JComponent JFrame JLabel Timer)
)

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
        :game-time 0.0
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
        :next-fire-time 0.0
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
    "Updates only the game state by the given timestep in wall seconds.  Takes
    into consideration action keys, but not 'meta' keys like rewind or pause."
    [state time-step keys-pressed]
    (let [
        time-step (* time-step (if (contains? keys-pressed KeyEvent/VK_S) slow-mo-speed 1.0))
        new-game-time (+ (:game-time state) time-step)
        spawn-new-bullet (and (contains? keys-pressed KeyEvent/VK_SPACE) (>= new-game-time (:next-fire-time state)))
        state
            (assoc state
                :game-time new-game-time
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
                        (+ new-game-time fire-delay)
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

(defn system-step
    "Updates the game state, history states, etc by the given timestep in seconds.  Takes
    into consideration all player keys including 'action' and 'meta' keys."
    [state previous-states time-step keys-pressed]
    (cond
        (empty? previous-states)
            (let [new-state (assoc state :game-time 0.0)]
                [new-state [new-state]])
        (contains? keys-pressed KeyEvent/VK_R)
            (let [
                new-game-time (max 0.0 (- (:game-time state) (* rewind-speed time-step)))
                new-previous-states (drop-until
                    (fn [s] (>= new-game-time (:game-time s)))
                    previous-states)
                new-game-state
                    (game-step
                        (first new-previous-states)
                        (- new-game-time (:game-time (first new-previous-states)))
                        {})]
                [new-game-state new-previous-states])
        (or (= time-step 0.0) (contains? keys-pressed KeyEvent/VK_P))
            [state previous-states]
        :else
            [
                (game-step state time-step keys-pressed)
                previous-states]))


