module Main where

    myGet m a b c = Just m >>= lookup a >>= lookup b >>= lookup c >>= return

    data Coord = C (Integer, Integer) deriving (Show)
    data Node = N [Coord]  deriving (Show)
    data Maze = M [(Coord, Node)] deriving (Show)

    instance Eq Coord where
        C a == C b = a == b

    myGetNode :: Maze -> Coord -> Maybe Node
    myGetNode (M m) c = lookup c m

    someMaze :: Maze
    someMaze =
        M [
            (C (1, 1), N [C (1, 2), C (2, 1)]),

            (C (1, 2), N [C (1, 3)]),
            (C (1, 3), N [C (1, 4)]),
            (C (1, 4), N [C (1, 5)]),
            (C (1, 5), N [C (1, 6)]),
            (C (1, 6), N []),

            (C (2, 1), N [C (3, 1)]),
            (C (3, 1), N [C (4, 1)]),
            (C (4, 1), N [C (5, 1)]),
            (C (5, 1), N [C (6, 1)]),
            (C (6, 1), N [])
        ]

    mySolveMaze :: Maze -> Coord -> [Coord]
    mySolveMaze m start =
        let nextFn = (\c -> case myGetNode m c of
                                            Nothing -> []
                                            Just (N r) -> r)
        in [start] >>= nextFn >>= nextFn >>= nextFn >>= return

