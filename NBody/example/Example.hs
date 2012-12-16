import Graphics.Rendering.OpenGL
import Graphics.UI.GLUT
import Data.IORef


main = do
    (progname,_) <- getArgsAndInitialize
    initialDisplayMode $= [WithDepthBuffer, DoubleBuffered]
    createWindow "Hello World"
    reshapeCallback $= Just reshape
    depthFunc $= Just Less
    keyboardMouseCallback $= Just keyboardMouse
    angle <- newIORef 0.0
    displayCallback $= (display angle)
    idleCallback $= Just (idle angle)
    mainLoop


display angle = do
    clear [ColorBuffer, DepthBuffer]
    loadIdentity
    a <- get angle
    rotate a $ Vector3 0 0 (1::GLfloat)
    scale 0.7 0.7 (0.7::GLfloat)
    preservingMatrix $ do
        mapM_ (\(x,y,z) -> preservingMatrix $ do
            color $ Color3 ((x+1.0)/2.0) ((y+1.0)/2.0) ((z+1.0)/2.0)
            translate $ Vector3 x y z
            rotate (a*3) $ Vector3 (1::GLfloat) (1::GLfloat) 0
            cube (0.1::GLfloat)
            color $ Color3 (0.0::GLfloat) (0.0::GLfloat) (0.0::GLfloat) --set outline color to black
            cubeFrame (0.1::GLfloat)
            ) $ points 7
    swapBuffers

idle angle = do
    a <- get angle
    angle $=! (a + 0.1) -- The parens are necessary due to a precedence bug in StateVar
    postRedisplay Nothing -- Only required on Mac OS X, which double-buffers internally


reshape s@(Size w h) = do
    viewport $= (Position 0 0, s)

keyboardMouse key state modifiers position = return ()

vertify3 :: [(GLfloat,GLfloat,GLfloat)] -> IO ()
vertify3 verts = sequence_ $ map (\(a,b,c) -> vertex $ Vertex3 a b c) verts

cube w = renderPrimitive Quads $ vertify3 [
    ( w, w, w), ( w, w,-w), ( w,-w,-w), ( w,-w, w),
    ( w, w, w), ( w, w,-w), (-w, w,-w), (-w, w, w),
    ( w, w, w), ( w,-w, w), (-w,-w, w), (-w, w, w),
    (-w, w, w), (-w, w,-w), (-w,-w,-w), (-w,-w, w),
    ( w,-w, w), ( w,-w,-w), (-w,-w,-w), (-w,-w, w),
    ( w, w,-w), ( w,-w,-w), (-w,-w,-w), (-w, w,-w) ]


points :: Int -> [(GLfloat,GLfloat,GLfloat)]
points n' = let n = fromIntegral n' in
    map (\k -> let t = 2*pi*k/n in (sin(t),cos(t),0.0))  [1..n]

cubeFrame w = renderPrimitive Lines $ vertify3 [
    ( w,-w, w), ( w, w, w),  ( w, w, w), (-w, w, w),
    (-w, w, w), (-w,-w, w),  (-w,-w, w), ( w,-w, w),
    ( w,-w, w), ( w,-w,-w),  ( w, w, w), ( w, w,-w),
    (-w, w, w), (-w, w,-w),  (-w,-w, w), (-w,-w,-w),
    ( w,-w,-w), ( w, w,-w),  ( w, w,-w), (-w, w,-w),
    (-w, w,-w), (-w,-w,-w),  (-w,-w,-w), ( w,-w,-w) ]

