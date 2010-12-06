; :mode=clojure:

(import
    '(java.awt Color)
    '(java.awt Dimension)
    '(javax.swing.JFrame)
    '(javax.swing.JComponent)
    '(javax.swing.JLabel)
    '(java.awt.event.ActionListener)
)

(defn create-game []
    (let [keys-pressed (ref #{})
        result
        (doto
            (proxy [javax.swing.JComponent] []
                (paint [g]
                    (do
                        (.setColor g (if (empty? (deref keys-pressed)) (java.awt.Color/RED) (java.awt.Color/BLUE)))
                        (let [rect (.getClipBounds g)]
                            (.fillRect g (.x rect) (.y rect) (.width rect) (.height rect))))))
            (.setPreferredSize (new java.awt.Dimension 640 480))
            (.setFocusable true)
            (.addKeyListener (proxy [java.awt.event.KeyAdapter] []
                (keyPressed [event] (if (not (.isConsumed event)) (do
                    (dosync
                        (alter keys-pressed (fn [x] (conj x (.getKeyCode event)))))
                    (.consume event))))
                (keyReleased [event] (if (not (.isConsumed event)) (do
                    (dosync
                        (alter keys-pressed (fn [x] (disj x (.getKeyCode event))))))))))
        )]
        (do
            (doto
                (new javax.swing.Timer (/ 1000 60)
                    (reify java.awt.event.ActionListener
                        (actionPerformed [this event] (.repaint result))))
                (.start))
            result)))

(let [frame (new javax.swing.JFrame)]
    (do
        (.add (.getContentPane frame) (create-game))
        (.pack frame)
        (.setResizable frame false)
        (.show frame)))

