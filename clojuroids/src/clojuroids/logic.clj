; :mode=clojure:

;; # Game logic #
;; I use the term 'thing' to refer to various objects that have position, velocity, etc..

(ns clojuroids.logic
    (:use
        [clojuroids [constants] [utilities]]
        [clojure.contrib.seq :only [separate]]))

(import
    '(java.awt Color Dimension Polygon RenderingHints)
    '(java.awt.event ActionListener KeyAdapter KeyEvent)
    '(javax.swing JComponent JFrame JLabel Timer)
)

(def
    ^{:doc "An AWT polygon which is used as the shape of the player's ship on screen."}
    player-shape
    (doto
        (new Polygon)
        (.addPoint 10 0)
        (.addPoint -3 5)
        (.addPoint -3 -5)))

(def
    ^{:doc "An AWT polygon which is used as the shape of each bullet on the screen."}
    bullet-shape
    (doto
        (new Polygon)
        (.addPoint 3 0)
        (.addPoint -3 1.5)
        (.addPoint -3 -1.5)))

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
    "Determines whether the given things have collided.  This is done by testing whether
    one object's center is within the other object's polygon (or vise versa).  This is an approximation
    which works so long as one of the objects is small and point-like."
    [thing1 thing2]
    (let [test-fn
        (fn [a b]
            (let [
                ;; First transform the thing into a frame of reference local to the asteroid.
                ;; This allows us to use the AWT Polgon's '.contains()' method.
                [x y] (rotate
                    (- (:x b) (:x a))
                    (- (:y b) (:y a))
                    (:a a))
                p (:poly a)]
                (.contains p x y)))]
        (or (test-fn thing1 thing2) (test-fn thing2 thing1))))

(defn average
    "Given the polar radii for an asteroid, compute the average radius.  This is used
    when determining if a given asteroid is too small and should simply be annihilated."
    [radii]
    (/ (apply + radii) (count radii)))

(defn generate-asteroid
    "Generates a random asteroid from fresh."
    [x y radius special]
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
            :sparkle-color Color/LIGHT_GRAY
            :special special
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
                (generate-asteroid (+ (:x asteroid) xo) (+ (:y asteroid) yo) (* current-radius 0.6) (:special asteroid))
                (generate-asteroid (- (:x asteroid) xo) (- (:y asteroid) yo) (* current-radius 0.4) (:special asteroid))
            ])))

(defn explode
    "Creates sparkles as if the given thing was exploding. The size is the initial radius of the explosion."
    [thing size]
    (take (Math/ceil (* size sparkle-explosion-amount)) (repeatedly (fn []
        (let [
            o (rand (* 2.0 Math/PI))
            a (rand (* 2.0 Math/PI))
            v (rand size)]
            (assoc thing
                :acc 0.0
                :eff sparkle-efficiency
                :x (+
                    (:x thing)
                    (* v (Math/cos o)))
                :y (+
                    (:y thing)
                    (* v (Math/sin o)))
                :xv (+
                    (:xv thing)
                    (* sparkle-explosion-amount v (Math/cos a)))
                :yv (+
                    (:yv thing)
                    (* sparkle-explosion-amount v (Math/sin a)))))))))


(defn filter-collisions
    "Given a list of things and asteroids, will detect collisions, split asteroids
    that need splitting, and remove the associated things."
    [bullets asteroids]
    (let [
        [survived hit]
            (separate
                (fn [a] (nil? (some
                    (fn [b] (collided? b a))
                    bullets)))
                asteroids)]
        [
            (filter
                (fn [b] (nil? (some
                    (fn [a] (collided? b a))
                    asteroids)))
                bullets)
            (apply concat (cons survived (map split-asteroid hit)))
            (apply concat (map (fn [h] (explode h (average (:radii h)))) hit))
        ]))

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
            :sparkle-color Color/RED
            :poly player-shape}
        :next-fire-time 0.0
        :asteroids
            (take 10 (repeatedly #(generate-asteroid (myrand 200 width) (rand height) initial-asteroid-size (zero? (rand-int 3)))))
        :sparkles []
    } )

(defn player-step
    "Updates the player object by the given timestep, applying whatever movements are indicated by
    the currently pressed set of keyboard keys."
    [player time-step keys-pressed]
    (if (nil? player) nil
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
        player)))

(defn new-bullet
    "Creates a new bullet object being released from the player's ship."
    [player]
    (assoc player
        :eff bullet-efficiency
        :acc bullet-acceleration
        :live-time bullet-live-time
        :sparkle-color Color/PINK
        :poly bullet-shape))

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

(defn bullet-step
    "Steps a bullet forward in time by the given timestep."
    [bullet time-step]
    (step-thing
        (assoc bullet
            :live-time (- (:live-time bullet) time-step))
        time-step))

(defn bullet-is-alive
    "Determines if a bullet should still be considered alive, or if it has flown for the maximum allowed time."
    [bullet]
    (> (:live-time bullet) 0))

(defn game-step
    "Updates only the game state by the given timestep in wall seconds.  Takes
    into consideration action keys, but not 'meta' keys like rewind or pause, or even
    object collisions."
    [state time-step keys-pressed]
    (let [
        time-step (* time-step (if (contains? keys-pressed KeyEvent/VK_S) slow-mo-speed 1.0))
        new-game-time (+ (:game-time state) time-step)
        spawn-new-bullet (and (not (nil? (:player state))) (contains? keys-pressed KeyEvent/VK_SPACE) (>= new-game-time (:next-fire-time state)))]
        (assoc state
            :game-time new-game-time
            :player (player-step (:player state) time-step keys-pressed)
            :asteroids (doall (map (fn [a] (step-thing a time-step)) (:asteroids state)))
            :bullets
                (doall (map (fn [b] (bullet-step b time-step))
                    (concat
                        (if spawn-new-bullet
                            [(new-bullet (:player state))]
                            [])
                    (filter bullet-is-alive (:bullets state)))))
            :next-fire-time
                (if spawn-new-bullet
                    (+ new-game-time fire-delay)
                    (:next-fire-time state))
            :sparkles
                (doall (take sparkle-limit
                    (map (fn [s] (step-thing s time-step))
                        (concat
                            (make-new-sparkles (concat (to-vec-if-not-nil (:player state)) (:bullets state)) time-step)
                            (filter sparkle-is-alive (:sparkles state)))))))))

(defn filter-player-collision
    "Determines if the player has collided with an asteroid.  Returns the player (nil on collision),
    remaining asteroids, and new sparkles coming from any collisions (usually empty)."
    [player asteroids]
    (let [[fp fa new-sparkles] (filter-collisions (to-vec-if-not-nil player) asteroids)]
        [
            (if (empty? fp) nil (first fp))
            fa
            new-sparkles
        ]))

(defn collisions-step
    "Processes a game state for object collisions.  This is not done as part
    of 'game-step' as in the forward prediction used in the rewind we don't want
    the differing PRNG outputs to show different collision results."
    [state]
    (let [
        [fb fa new-sparkles1]
            (filter-collisions (:bullets state) (:asteroids state))
        [new-player fa new-sparkles2]
            (filter-player-collision (:player state) fa)
        ]
        (assoc state
            :player new-player
            :sparkles
                (concat new-sparkles1 new-sparkles2 (:sparkles state))
            :bullets fb
            :asteroids fa)))

(defn backport-state
    "Combines an old state with a newer state, taking care to preserve the special
    asteroids from the newer state."
    [old-state new-state]
    (assoc old-state
        :asteroids
        (concat
            (filter (complement :special) (:asteroids old-state))
            (filter :special (:asteroids new-state)))))

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
                    (backport-state
                        (game-step
                            (first new-previous-states)
                            (- new-game-time (:game-time (first new-previous-states)))
                            {})
                    state)]
                [new-game-state new-previous-states])
        (or (= time-step 0.0) (contains? keys-pressed KeyEvent/VK_P))
            [state previous-states]
        :else
            [
                (collisions-step (game-step state time-step keys-pressed))
                previous-states]))


