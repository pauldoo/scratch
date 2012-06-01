class BasicEq a where
        isEqual :: a -> a -> Bool

instance BasicEq Bool where
        isEqual True True = True
        isEqual False False = True
        isEqual _ _ = False


class BasicEq3 a where
        isEqual3 :: a -> a -> Bool
        isEqual3 x y = not (isNotEqual3 x y)
        isNotEqual3 :: a -> a -> Bool
        isNotEqual3 x y = not (isEqual3 x y)


data Color = Red | Green | Blue

instance BasicEq3 Color where
        isEqual3 Red Red = True
        isEqual3 Green Green = True
        isEqual3 Blue Blue = True
        isEqual3 _ _ = False

