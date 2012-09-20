{-# LANGUAGE FunctionalDependencies, MultiParamTypeClasses #-}
module MonadHandleIO where
import MonadHandle
import qualified System.IO

import Control.Monad.Trans (MonadIO(..), MonadTrans(..))
import System.Directory (removeFile)

instance MonadHandle System.IO.Handle IO where
    openFile = System.IO.openFile
    hPutStr = System.IO.hPutStr
    hClose = System.IO.hClose
    hGetContents = System.IO.hGetContents
    hPutStrLn = System.IO.hPutStrLn

