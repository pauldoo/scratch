module Main () where

import JSONClass
import PrettyJSON
import Prettify

main = putStrLn (pretty 10 value) where
        value = renderJValue (toJValue (JObj [("f", JNumber 1), ("q", JNumber 10)]))


