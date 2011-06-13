; :mode=clojure:

;; # AWT/Swing graphics #

(ns clojuroids.render
    (:use
        [clojuroids [constants] [utilities]]))

(import
    '(java.awt Color Dimension RenderingHints)
)

;(defn make-opaque [c o]
;    (new Color
;        (float (/ (.getRed c) 255.0))
;        (float (/ (.getGreen c) 255.0))
;        (float (/ (.getBlue c) 255.0))
;        (float o)))

(defn draw-player
    "Draws the player's ship to the given AWT Graphics2D object."
    [player g opacity-fn]
    (if (>= (opacity-fn player) 0.5)
        (do
            (.setColor g (Color/WHITE))
            (let [old-transform (.getTransform g)]
                (.translate g (:x player) (:y player))
                (.rotate g (:a player))
                (.fill g (:poly player))
                (.setTransform g old-transform)))))

(defn draw-asteroid
    "Draws an asteroid object to the given AWT Graphics2D object."
    [asteroid g opacity-fn]
    (if (>= (opacity-fn asteroid) 0.5)
        (do
            (.setColor g (if (:special asteroid) Color/CYAN Color/GRAY))
            (let [old-transform (.getTransform g)]
                (.translate g (:x asteroid) (:y asteroid))
                (.rotate g (:a asteroid))
                (.fill g (:poly asteroid))
                (.setTransform g old-transform)))))

(defn draw-bullet
    "Draws a bullet object to the given AWT Graphics2D object."
    [bullet g opacity-fn]
    (if (>= (opacity-fn bullet) 0.5)
        (do
            (.setColor g (Color/YELLOW))
            (let [old-transform (.getTransform g)]
                (.translate g (:x bullet) (:y bullet))
                (.rotate g (:a bullet))
                (.fill g (:poly bullet))
                (.setTransform g old-transform)))))

(defn draw-sparkle
    "Draws a sparkle object to the given AWT Graphics2D object."
    [sparkle g opacity-fn]
    (if (>= (opacity-fn sparkle) 0.5)
        (do
            (.setColor g (:sparkle-color sparkle))
            (let [old-transform (.getTransform g)]
                (.translate g (:x sparkle) (:y sparkle))
                (.fillRect g 0 0 1 1)
                (.setTransform g old-transform)))))

(defn wrapped-distance
    "The shortest distance between two objects taking into account screen wrapping."
    [{x1 :x y1 :y} {x2 :x y2 :y}]
    (let
        [
            dx (Math/abs (- x1 x2))
            dy (Math/abs (- y1 y2))
        ]
        (mag
            (Math/min dx (- width dx))
            (Math/min dy (- height dy)))))

(defn linear-ramp
    "Linearly map 'x' so that 'a' would become 0.0 and 'b' would become 1.0 (clamped).
    Not quite true, as the 'a' value is pushed away from 'b' slightly so that neighbouring
    ramps overlap in some ad-hoc way.."
    [x a b]
    (let [a (+ (* (- a b) 1.5) b)]
        (Math/max 0.0 (Math/min 1.0
            (/ (- x a) (- b a))))))

(defn opacity
    "Takes a single object and determines opacity based on whether it is within
    [min-time max-time] light seconds away from the view point."
    [
        object
        viewpoint
        min-time
        peak-time
        max-time
    ]
    (let [distance-in-light-seconds (/ (wrapped-distance object viewpoint) speed-of-light)]
        (cond
            (and
                (<= min-time distance-in-light-seconds)
                (<= distance-in-light-seconds peak-time))
                (linear-ramp distance-in-light-seconds min-time peak-time)
            (and
                (<= peak-time distance-in-light-seconds)
                (<= distance-in-light-seconds max-time))
                (linear-ramp distance-in-light-seconds max-time peak-time)
            :else 0.0)))

(defn single-state-render
    "Takes a single game state, and renders the portions which are within a certain
    min and max age away from the viewpoint."
    [state g viewpoint min-age peak-age max-age]
    (assert (< min-age peak-age))
    (assert (< peak-age max-age))
    (let [
        opacity-fn (fn [obj] (opacity obj viewpoint min-age peak-age max-age))]
        (do
            (doseq [s (:sparkles state)] (draw-sparkle s g opacity-fn))
            (doseq [a (:asteroids state)] (draw-asteroid a g opacity-fn))
            (doseq [b (:bullets state)] (draw-bullet b g opacity-fn))
            (if (not (nil? (:player state)))
                (draw-player (:player state) g opacity-fn)))))

(defn history-render
    "Takes a sequence of game states, and renders them as far back as necessary
    given the speed of light and the size of the board."
    [states g viewpoint viewtime newer-state]
    (if (not (empty? states))
        (let [
            state (first states)
            older-state (second states)

            peak-age (- viewtime (:game-time state))
            min-age (if (not (nil? newer-state)) (- viewtime (:game-time newer-state)) (- peak-age 0.05))
            max-age (if (not (nil? older-state)) (- viewtime (:game-time older-state)) (+ peak-age 0.05))]
            (if
                (<= peak-age
                    (/ (mag (/ width 2.0) (/ height 2.0)) speed-of-light))
                (do
                    (single-state-render state g viewpoint min-age peak-age max-age)
                    (history-render (rest states) g viewpoint viewtime state))))))

(defn game-render
    "Takes a list of game states consisting of the player ship, list of asteroids, list of bullets, and
    list of sparkles; and draws them all to the given AWT Graphics2D object.
    A sequence of states is required because drawing will take into account some relativistic
    effects.  These effects are computed relative to the position of the player in the first (most
    recent) state."
    [states g]
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
        (let [
            viewpoint (value-or-default (:player (first states)) {:x (/ width 2.0) :y (/ height 2.0)})
            viewtime (:game-time (first states))
            ]
            (history-render states g viewpoint viewtime nil))
        (if (nil? (:player (first states)))
            (do
                (.setColor g (Color/RED))
                (.drawString g "Game Over!" (/ width 2) (/ height 2))))))

