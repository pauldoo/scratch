#!/usr/bin/env io

# Seven languages in seven weeks, Io, day2, Q3

sampleList := list(
    list(1, 2, 3),
    list(4, 5, 6),
    list(7, 8, 9))
    
sampleList println

List deepSum := method(self flatten sum)

sampleList deepSum println    

