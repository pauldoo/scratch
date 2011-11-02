#!/usr/bin/env io

# Seven languages in seven weeks, Io, day2, Q6

List2D := Object clone

repeat := method(n, v,
    Range 1 to(n) asList map(z, v))


List2D dim := method(x, y,
    r := List2D clone
    r data := Range 1 to(y) asList map(z, repeat(x, nil))
    r)
    
List2D set := method(x, y, v, data at(y) atPut(x, v))
List2D get := method(x, y, data at(y) at(x))
List2D width := method(data at(0) size)
List2D height := method(data size)

List2D transpose := method(
    r := List2D dim(height, width)
    for (x, 0, (width-1),
        for (y, 0, (height-1),
            r set(y, x, get(x, y))))
    r)

sample := List2D dim(3, 2)
sample width println
sample height println
sample println

sample set(0, 0, 1)
sample set(1, 0, 2)
sample set(2, 0, 3)
sample set(0, 1, 4)
sample set(1, 1, 5)
sample set(2, 1, 6)

sample println

sample transpose println

