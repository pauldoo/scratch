; :mode=clojure:

;; # AWT/Swing graphics #

(ns clojuroids.render)

(import
    '(java.awt Color Dimension Polygon RenderingHints)
)

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




