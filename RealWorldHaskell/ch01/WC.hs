main = interact wordCount
        where wordCount input =
                (show (length (lines input)) ++ " lines\n") ++
                (show (length (words input)) ++ " words\n") ++
                (show (length input) ++ " characters\n")

