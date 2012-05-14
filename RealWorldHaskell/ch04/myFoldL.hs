-- foldl in terms of foldr

-- myfoldl f a (x:xs) = myfoldl f (f a x) xs
-- myfoldl _ a [] = a

myfoldr f a (x:xs) = f x (myfoldr f a xs)
myfoldr _ a [] = a


myfoldl f z xs = (myfoldr step id xs) z
        where
                step x g a = g (f a x)

myconcat ls = foldr (++) [] ls

