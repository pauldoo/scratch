module RandomSupply where
import Supply
import System.Random (split, randoms, getStdRandom, Random)

randomsIO :: Random a => IO [a]
randomsIO =
    getStdRandom $ \g ->
        let (a, b) = split g
        in (randoms a, b)

-- (fst . runSupply Supply.next) `fmap` randomsIO

