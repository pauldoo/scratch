module Parse where

import qualified Data.ByteString.Lazy.Char8 as L8
import qualified Data.ByteString.Lazy as L
import Data.Int
import Data.Word


data ParseState = ParseState {
        string :: L.ByteString,
        offset :: Int64
    } deriving (Show)

simpleParse :: ParseState -> (a, ParseState)
simpleParse = undefined

betterParse :: ParseState -> Either String (a, ParseState)
betterParse = undefined

newtype Parse a = Parse {
    runParse :: ParseState -> Either String (a, ParseState)
    }

identity :: a -> Parse a
identity a = Parse (\s -> Right (a, s))

parse :: Parse a -> L.ByteString -> Either String a
parse parser initState =
    case runParse parser (ParseState initState 0) of
        Left err -> Left err
        Right (result, _) -> Right result

{--
Prelude> :load Parse
Ok, modules loaded: Parse.
Prelude Parse> :type parse (identity 1) undefined
parse (identity 1) undefined :: (Num t) => Either String t
Prelude Parse> parse (identity 1) undefined
Loading package bytestring-0.9.1.4 ... linking ... done.
Right 1
Prelude Parse> parse (identity "foo") undefined
Right "foo"
--}

parseByte :: Parse Word8
parseByte =
    getState ==> \initState ->
    case L.uncons (string initState) of
        Nothing ->
            bail "no more input"
        Just (byte, remainder) ->
                putState newState ==> \_ ->
                identity byte
            where
                newState = initState {
                    string = remainder,
                    offset = newOffset }
                newOffset = offset initState + 1

getState :: Parse ParseState
getState = Parse (\s -> Right (s, s))

putState :: ParseState -> Parse ()
putState s = Parse (\_ -> Right ((), s))

bail :: String -> Parse a
bail err = Parse $ \s -> Left $ "byte offset " ++ show (offset s) ++ ": " ++ err

(==>) :: Parse a -> (a -> Parse b) -> Parse b

firstParser ==> secondParser =
        Parse chainedParser
    where
        chainedParser initState =
            case runParse firstParser initState of
                Left errMessage ->
                    Left errMessage
                Right (firstResult, newState) ->
                    runParse (secondParser firstResult) newState

