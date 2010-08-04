#!/usr/bin/env clj

(import
    '(java.awt Color)
    '(java.awt.image BufferedImage)
    '(java.io File)
    '(javax.imageio ImageIO)
)

;(defn step [zr zi cr ci] [

(let
    [image (new BufferedImage 640 480 BufferedImage/TYPE_INT_ARGB)]
    (do
        (let
            [g (.getGraphics image)]
            (dorun
                (for
                    [x (range 0 640) y (range 0 480)]
                    (doto g
                        (.setColor (new Color (float (/ x 640)) (float 0.3) (float 0.1)))
                        (.fillRect x y 1 1)
                    )
                )
            )
        )
        (ImageIO/write image "png" (File. "result.png"))
    )
)


