module RandomGolf where
import Control.Arrow (first)
import System.Random (Random, randoms, getStdRandom, split)

randomsIO_golfed :: Random a => IO [a]
randomsIO_golfed = getStdRandom (first randoms . split)


