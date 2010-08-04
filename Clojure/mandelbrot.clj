#!/usr/bin/env clj

(import
    '(java.awt Color)
    '(java.awt.image BufferedImage)
    '(java.io File)
    '(javax.imageio ImageIO)
)

(defn step [zr zi cr ci]
    [
        (double (+ (* zr zr) (-(* zi zi)) cr))
        (double (+ (-(* zr zi 2) ci)))
    ])

(defn dopixel [max-iters cr ci]
    (loop [count 0 zr 0 zi 0]
        (if
            (or
                (>= count max-iters)
                (>= (+ (* zr zr) (* zi zi)) 10))
            count
            (let
                [[zr zi] (step zr zi cr ci)]
                (recur (inc count) zr zi)))))

(let
    [
        width 640
        height 480
        min-x -2.5
        max-x 1.5
        min-y -1.5
        max-y 1.5
        max-iters 20
        image (new BufferedImage width height BufferedImage/TYPE_INT_ARGB)
    ]
    (do
        (let
            [g (.getGraphics image)]
            (dorun
                (for
                    [x (range 0 width) y (range 0 height)]
                    (let
                        [count (dopixel max-iters
                            (+ min-x (* (/ x 640) (- max-x min-x)))
                            (+ min-y (* (/ y 480) (- max-y min-y))))]
                        (doto g
                            (.setColor (new Color (float (/ x width)) (float (/ count max-iters)) (float 0.1)))
                            (.fillRect x y 1 1))))))
        (ImageIO/write image "png" (File. "result.png"))))

