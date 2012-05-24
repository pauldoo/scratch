module Main () where

import SimpleJSON
import PrettyJSON
import Prettify

main = putStrLn (pretty 10 value) where
        value = renderJValue (JObject [("f", JNumber 1), ("q", JBool True)])


