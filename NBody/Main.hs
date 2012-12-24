import Data.IORef
import Graphics.Rendering.OpenGL
import Graphics.UI.GLUT

import State
import Simulation
import Display


main :: IO ()
main = do
    (progname,_) <- getArgsAndInitialize

    initialDisplayMode $= [WithDepthBuffer, DoubleBuffered, Multisampling]
    createWindow "Hello World"
    multisample $= Enabled
    pointSmooth $= Enabled
    hint PointSmooth $= Nicest

    reshapeCallback $= Just reshape
    depthFunc $= Just Less

    keyboardMouseCallback $= Just keyboardMouse
    state <- newIORef initialState
    displayCallback $= (display state)
    idleCallback $= Just (idle state)
    mainLoop

reshape s@(Size w h) = do
    viewport $= (Position 0 0, s)

idle :: IORef State -> IO ()
idle state = do
    s <- get state
    state $=! ((iterate (updateState timestep) s) !! updatesPerFrame)
    postRedisplay Nothing

keyboardMouse key state modifiers position =
    return ()

timestep :: Double
timestep = 0.01

updatesPerFrame = 1

