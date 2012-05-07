splitLines [] = []
splitLines cs =
        let (pre, suf) = break isLineTerminator cs
        in pre : case suf of
                ('\r':'\n':rest) -> splitLines rest
                ('\r':rest) -> splitLines rest
                ('\n':rest) -> splitLines rest
                [] -> []

isLineTerminator c = c == '\r' || c == '\n'

