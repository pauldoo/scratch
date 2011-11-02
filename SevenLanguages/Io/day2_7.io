#!/usr/bin/env io

# Seven languages in seven weeks, Io, day2, Q7

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
    
List2D saveToFile := method(filename,
    File with(filename) open write(self serialized) close)
    
List2D loadFromFile := method(filename,
    r := doRelativeFile(filename)
    r)    

sample := List2D dim(3, 2)
sample set(0, 0, 1)
sample set(1, 0, 2)
sample set(2, 0, 3)
sample set(0, 1, 4)
sample set(1, 1, 5)
sample set(2, 1, 6)
sample println

sample saveToFile("test.txt")

List2D loadFromFile("test.txt") transpose println

