module Ex5_7 where

-- Q 7 - implement dot product

dotProduct :: Num a => [a] -> [a] -> a
dotProduct xs ys = sum [x * y | (x, y) <- (zip xs ys)]


