{-# LANGUAGE GeneralizedNewtypeDeriving, TypeSynonymInstances, FlexibleInstances, MultiParamTypeClasses #-}

module WriterIO where
import Control.Monad.Writer
import System.IO (IOMode)

import MonadHandle
import SafeHello

data Event =
    Open FilePath IOMode |
    Put String String |
    Close String |
    GetContents String
    deriving (Show)


newtype WriterIO a = W { runW :: Writer [Event] a } deriving (Monad, MonadWriter [Event])

runWriterIO :: WriterIO a -> (a, [Event])
runWriterIO = runWriter . runW


instance MonadHandle FilePath WriterIO where
    openFile path mode = tell [Open path mode] >> return path
    hPutStr h str = tell [Put h str]
    hClose h = tell [Close h]
    hGetContents h = tell [GetContents h] >> return ""

{--
runWriterIO (SafeHello.safeHello "foo")
((),[Open "foo" WriteMode,Put "foo" "hello world",Put "foo" "\n",Close "foo"])
--}

