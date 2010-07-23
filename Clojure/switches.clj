#!/usr/bin/env clj

; Solution to the 1000 switches problem
; http://programmerpuzzlers.com/which-switch/
; (it's cheating to code this, but it's an interesting exercise)

(println (count
    (filter
        (fn
            [n]
            (odd? (count
                (filter
                    #(zero? (rem n %))
                    (range 1 1001)))))
        (range 1 1001))))

(println
    (let
        [
            counter-fn
            (fn
                [n]
                (count
                    (filter
                        #(zero? (rem n %))
                        (range 1 1001))))
            switch-no
            (apply max-key counter-fn (range 1 1001))
        ]
        [switch-no (counter-fn switch-no)]))

