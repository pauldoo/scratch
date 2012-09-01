{-# LANGUAGE FlexibleInstances, FunctionalDependencies, MultiParamTypeClasses #-}

module SupplyClass(
    MonadSupply(..),
    S.Supply,
    S.runSupply) where
import qualified Supply as S

class (Monad m) => (MonadSupply s) m | m -> s where
    next :: m (Maybe s)

instance MonadSupply s (S.Supply s) where
    next = S.next

showTwo_class :: (Show s, Monad m, MonadSupply s m) => m String
showTwo_class = do
    a <- next
    b <- next
    return (show "a: " ++ show a ++ ", b: " ++ show b)

