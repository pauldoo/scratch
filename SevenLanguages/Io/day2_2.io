#!/usr/bin/env io

# Seven languages in seven weeks, Io, day2, Q2

divide := Number getSlot("/")

(4/5) println
(4/0) println

Number / := method(d,
    if (d == 0,
        0,
        self divide(d)))
        
(4/5) println
(4/0) println

