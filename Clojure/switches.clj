#!/usr/bin/env clj

; Solution to the 1000 switches problem
; http://programmerpuzzlers.com/which-switch/
; (it's cheating to code this, but it's an interesting exercise)

(println
    (count (filter
       identity
       (map
           (fn
               [n]
               (odd? (count (filter
                   identity
                   (map
                       (fn [val] (zero? (rem n val)))
                       (range 1 1001))))))
           (range 1 1001)))))

