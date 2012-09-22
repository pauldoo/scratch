module Ex8(parse, expr, eval) where
--module Ex8 where
import Data.Char (isDigit, isSpace)

newtype Parser a = P (String -> [(a, String)])

failure :: Parser a
failure = P (\inp -> [])

parse :: Parser a -> String -> [(a, String)]
parse (P p) inp = p inp

item :: Parser Char
item = P (\inp -> case inp of
    [] -> []
    (x : xs) -> [(x, xs)])

instance Monad Parser where
    return v = P (\inp -> [(v, inp)])
    p >>= f = P (\inp -> case parse p inp of
        [] -> []
        [(v, out)] -> parse (f v) out)

(+++) :: Parser a -> Parser a -> Parser a
p +++ q = P (\inp -> case parse p inp of
    [] -> parse q inp
    [(v, out)] -> [(v, out)])

sat :: (Char -> Bool) -> Parser Char
sat p =
    do
        x <- item
        if p x then return x else failure

digit :: Parser Char
digit = sat isDigit

char :: Char -> Parser Char
char x = sat (== x)

string :: String -> Parser String
string [] = return []
string (x : xs) =
    do
        char x
        string xs
        return (x : xs)

many :: Parser a -> Parser [a]
many p = many1 p +++ return []

many1 :: Parser a -> Parser [a]
many1 p =
    do
        v <- p
        vs <- many p
        return (v : vs)

nat :: Parser Int
nat =
    do
        xs <- many1 digit
        return (read xs)

space :: Parser ()
space =
    do
        many (sat isSpace)
        return ()

token :: Parser a -> Parser a
token p =
    do
        space
        v <- p
        space
        return v

natural :: Parser Int
natural = token nat

symbol :: String -> Parser String
symbol xs = token (string xs)

expr :: Parser Int
expr =
    do
        t <- term
        (do
            symbol "+"
            e <- expr
            return (t + e)) +++ return t

term :: Parser Int
term =
    do
        f <- factor
        (do
            symbol "*"
            t <- term
            return (f * t)) +++ return f

factor :: Parser Int
factor =
    do
        symbol "("
        e <- expr
        symbol ")"
        return e
    +++ natural

eval :: String -> Int
eval xs = case parse expr xs of
    [(n, [])] -> n
    [(_, out)] -> error ("unused input " ++ out)
    [] -> error "invalid input"


