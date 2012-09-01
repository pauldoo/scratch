{-# LANGUAGE GeneralizedNewtypeDeriving #-}
module Supply (
    Supply,
    next,
    runSupply) where
import Control.Monad.State

newtype Supply s a = S (State [s] a) deriving (Monad)

runSupply :: Supply s a -> [s] -> (a, [s])
next :: Supply s (Maybe s)

next = S $ do
    st <- get
    case st of
        [] -> return Nothing
        (x:xs) -> do
            put xs
            return (Just x)

runSupply (S m) xs = runState m xs

-- runSupply next [1, 2, 3]
-- runSupply (liftM2 (,) next next) [1, 2, 3]

showTwo :: (Show s) => Supply s String
showTwo = do
    a <- next
    b <- next
    return (show "a: " ++ show a ++ ", b: " ++ show b)

