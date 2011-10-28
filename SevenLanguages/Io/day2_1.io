#!/usr/bin/env io

# Seven languages in seven weeks, Io, day2, Q1

# Naive recursive fibonacci

fib_recur := method(arg,
    if (arg <= 2, 1,
        a := fib_recur(arg-2)
        b := fib_recur(arg-1)
        a + b))

fib_recur(1) println
fib_recur(2) println
fib_recur(8) println


# Iterative fibonacci
fib_iter := method(arg,
    a := 1
    b := 1
    while (arg > 2, 
        t := a
        a = b
        b = t + b
        arg = arg - 1)
    b)
    
    
fib_iter(1) println
fib_iter(2) println
fib_iter(8) println

Lobby println
    
