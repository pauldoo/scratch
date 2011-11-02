#!/usr/bin/env io

# Seven languages in seven weeks, Io, day2, Q8

target := Random value(1, 100) round
previous_error := 100
in := File standardInput
for(g, 1, 10,
    guess := in readLine asNumber
    error := (guess - target) abs
    if (error == 0, 
        "YOU WIN!",
        if (error < previous_error, "Hotter",
            if (error == previous_error, "Same temperature",
                "Colder"))) println
    if (error == 0, break)
    previous_error := error)

