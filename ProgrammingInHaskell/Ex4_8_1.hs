module Ex4_8_1 where

halve xs | even (length xs) = (take n xs, drop n xs) where n = (length xs) `div` 2


