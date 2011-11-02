#!/usr/bin/env io

# Seven languages in seven weeks, Io, day2, Q5

List2D := Object clone

repeat := method(n, v,
    Range 1 to(n) asList map(z, v))


List2D dim := method(x, y,
    r := List2D clone
    r data := Range 1 to(y) asList map(z, repeat(x, nil))
    r)
    
List2D set := method(x, y, v, data at(y) atPut(x, v))
List2D get := method(x, y, data at(y) at(x))

sample := List2D dim(4, 3)
sample println

sample get(1, 1) println
sample set(1, 1, 5)
sample get(1, 1) println
sample get(1, 2) println


