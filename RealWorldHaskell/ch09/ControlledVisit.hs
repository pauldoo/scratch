module ControlledVisit where

import Control.Monad (filterM)
import System.Directory (Permissions(..), getModificationTime, getPermissions, getDirectoryContents)
import System.Time (ClockTime(..))
import System.FilePath (takeExtension)
import Control.OldException (bracket, handle)
import System.IO (IOMode(..), hClose, hFileSize, openFile)
import System.FilePath((</>))
import Control.Monad (forM, liftM)

data Info = Info {
    infoPath :: FilePath,
    infoPerms :: Maybe Permissions,
    infoSize :: Maybe Integer,
    infoModTime :: Maybe ClockTime
    } deriving (Eq, Ord, Show)

maybeIO :: IO a -> IO (Maybe a)
maybeIO act = handle (\_ -> return Nothing) (Just `liftM` act)

getInfo :: FilePath -> IO Info
getInfo path = do
    perms <- maybeIO (getPermissions path)
    size <- maybeIO (bracket (openFile path ReadMode) hClose hFileSize)
    modified <- maybeIO (getModificationTime path)
    return (Info path perms size modified)


isDirectory :: Info -> Bool
isDirectory = maybe False searchable . infoPerms

traverse :: ([Info] -> [Info]) -> FilePath -> IO [Info]
traverse order path = do
    names <- getUsefulContents path
    contents <- mapM getInfo (path : map (path </>) names)
    liftM concat $ forM (order contents) $ \info -> do
        if isDirectory info && infoPath info /= path
            then traverse order (infoPath info)
            else return [info]

getUsefulContents :: FilePath -> IO [String]
getUsefulContents path = do
    names <- getDirectoryContents path
    return (filter (`notElem` [".", ".."]) names)

