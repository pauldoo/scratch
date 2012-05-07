import Data.List (partition)

-- Find smallest integer greater than 'n' not in the list.
-- List must not contain duplicates.
-- Executes in O(length of list).
minFree :: Int -> [Int] -> Int

minFree n lst = minFreeImp n (filter ((<=) n) lst) where
        minFreeImp n [] = n
        minFreeImp n lst =
                let
                        k = ((length lst) + 1) `div` 2
                        inRange v = n <= v && v < (n+k)
                        (a, b) = partition inRange lst
                in
                        if (length a) < k
                        then minFreeImp n a
                        else minFreeImp (n+k) b


