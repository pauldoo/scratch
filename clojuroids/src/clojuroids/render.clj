; :mode=clojure:

;; # AWT/Swing graphics #

(ns clojuroids.render
    (:use
        [clojuroids [constants]]))

(import
    '(java.awt Color Dimension RenderingHints)
)

(defn draw-player
    "Draws the player's ship to the given AWT Graphics2D object."
    [player g]
    (if (not (nil? player))
        (do
            (.setColor g (Color/WHITE))
            (let [old-transform (.getTransform g)]
                (.translate g (:x player) (:y player))
                (.rotate g (:a player))
                (.fill g (:poly player))
                (.setTransform g old-transform)))
        (do
            (.setColor g (Color/RED))
            (.drawString g "Game Over!" (/ width 2) (/ height 2)))))

(defn draw-asteroid
    "Draws an asteroid object to the given AWT Graphics2D object."
    [asteroid g]
    (do
        (.setColor g (if (:special asteroid) Color/CYAN Color/GRAY))
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
            (.fill g (:poly bullet))
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
        (let [viewpoint (:player (first states))]
            (doseq [state states]
                (doseq [s (:sparkles state)] (draw-sparkle s g))
                (doseq [a (:asteroids state)] (draw-asteroid a g))
                (doseq [b (:bullets state)] (draw-bullet b g))
                (draw-player (:player state) g)))
))




