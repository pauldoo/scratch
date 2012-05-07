data List a =
        Cons a (List a)
        | Nil

fromList (Cons x xs) = x:(fromList xs)
fromList Nil = []


data Tree a = Tree (Maybe (Tree a)) a (Maybe (Tree a)) deriving (Show)

