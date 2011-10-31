#!/usr/bin/env io

# Seven languages in seven weeks, Io, day2, Q4



List myAverage := method(
    self foreach(v, if(v isKindOf(Number), nil, Exception raise("Not a number")))
    (self reduce(+)) / (self size))

sampleList := list(1, 2, 3, 4, 5, 6)
sampleList println
sampleList myAverage println    

sampleList := list(4, 5, "foo")
sampleList println
sampleList myAverage println

