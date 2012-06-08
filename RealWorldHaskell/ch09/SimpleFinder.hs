module SimpleFind (simpleFind) where

import RecursiveContents (getRecursiveContents)

simpleFind :: (FilePath -> Bool) -> FilePath -> IO [FilePath]

simpleFind p path = do
    names <- getRecursiveContents path
    return (filter p names)

{-
Prelude> :load RecursiveContents
Prelude RecursiveContents> :load SimpleFinder
Prelude SimpleFind> :m +System.FilePath
Prelude SimpleFind System.FilePath> let t = simpleFind (\p -> takeExtension p == ".hs")
Prelude SimpleFind System.FilePath> :t t
t :: FilePath -> IO [FilePath]
Prelude SimpleFind System.FilePath> t ".."

-}

