module Display(display) where
import Graphics.Rendering.OpenGL
import Graphics.UI.GLUT
import Data.IORef

import State

display :: IORef State -> IO ()
display state = do
    clear [ColorBuffer, DepthBuffer]
    loadIdentity
    s <- get state
    renderPrimitive Points $ do
        mapM_ drawPoint (map location (stars s))
    swapBuffers

drawPoint :: Vec3 -> IO ()
drawPoint (Vec3 x y z) = do
    vertex $ Vertex3 x y z

