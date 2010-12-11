; :mode=clojure:

(import
    '(java.awt Color)
    '(java.awt Dimension)
    '(javax.swing.JFrame)
    '(javax.swing.JComponent)
    '(javax.swing.JLabel)
    '(java.awt.event.ActionListener)
)

(defn do-mod [coll func v]
    (dosync (alter coll (fn [x] (apply func [x v])))))

(defn create-component [game-state]
    (doto
        (proxy [javax.swing.JComponent] []
            (paint [g] (dosync
                (.setColor g (if (empty? (deref game-state)) (java.awt.Color/RED) (java.awt.Color/BLUE)))
                (let [rect (.getClipBounds g)]
                    (.fillRect g (.x rect) (.y rect) (.width rect) (.height rect))))))
        (.setPreferredSize (new java.awt.Dimension 640 480))
        (.setDoubleBuffered true)
        (.setFocusable true)))

(defn game-step [state keys-pressed]
    (if (empty? keys-pressed)
        (assoc (hash-map) :a true)
        (hash-map)))

(defn create-game []
    (let [
        keys-pressed (ref (hash-set))
        game-state (ref (hash-map))
        result (create-component game-state)
        ]
        (do
            (doto
                result
                (.addKeyListener (proxy [java.awt.event.KeyAdapter] []
                    (keyPressed [event] (if (not (.isConsumed event)) (do
                        (do-mod keys-pressed conj (.getKeyCode event))
                        (.consume event))))
                    (keyReleased [event] (if (not (.isConsumed event)) (do
                        (do-mod keys-pressed disj (.getKeyCode event))
                        (.consume event)))))))

            (doto
                (new javax.swing.Timer (/ 1000 60)
                    (reify java.awt.event.ActionListener
                        (actionPerformed [this event] (.repaint result))))
                (.start))

            (future (loop []
                (do
                    (Thread/sleep 1)
                    (dosync (alter game-state (fn [state] (game-step state (deref keys-pressed)))))
                    (recur))))

            result)))

(let [frame (new javax.swing.JFrame)]
    (do
        (.add (.getContentPane frame) (create-game))
        (.pack frame)
        (.setResizable frame false)
        (.show frame)))

