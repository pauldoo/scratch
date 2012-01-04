module Main where
    myEven 0 = True
    myEven n = not (myEven (n-1))

    allEven [] = True
    allEven (h:t) = (myEven h) && (allEven t)

    myReverse lst = foldl (\result next -> next:result) [] lst

    colourCombinations =
        let
            colours = ["black", "white", "blue", "yellow", "red"]
        in
            [(a, b) | a <- colours, b <- colours, a < b]

    multiplicationTable =
        let
            numbers = [1..12]
        in
            [(a, b, a*b) | a <- numbers, b <- numbers]

    mapColouring =
        let
            colours = ["red", "green", "blue"]
        in
            [(("Alabama", a), ("Florida", f), ("Georgia", g), ("Mississippi", m), ("Tennessee", t)) |
                a <- colours,
                f <- colours,
                g <- colours,
                m <- colours,
                t <- colours,
                m /= t,
                m /= a,
                a /= t,
                a /= m,
                a /= g,
                a /= f,
                g /= f,
                g /= t]

