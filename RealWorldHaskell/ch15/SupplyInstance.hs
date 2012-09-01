{-# LANGUAGE GeneralizedNewtypeDeriving, FlexibleInstances, MultiParamTypeClasses #-}
module SupplyInstance where
import SupplyClass

newtype Reader e a = R { runReader :: e -> a }

instance Monad (Reader e) where
    return a = R $ \_ -> a
    m >>= k = R $ \r -> runReader (k (runReader m r)) r

ask :: Reader e e
ask = R id

-- runReader (ask >>= \x -> return (x * 3)) 2

newtype MySupply e a = MySupply { runMySupply :: Reader e a } deriving (Monad)

instance MonadSupply e (MySupply e) where
    next = MySupply $ do
        v <- ask
        return (Just v)

xy :: (Num s, MonadSupply s m) => m s
xy = do
    Just x <- next
    Just y <- next
    return (x * y)

-- (fst . runSupply xy) `fmap` randomsIO

runMS :: MySupply i a -> i -> a
runMS = runReader . runMySupply

-- runMS xy 2
