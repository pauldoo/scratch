
import List (intersperse)

mandelBrotSample :: Double -> Double -> Int -> Int
mandelBrotSample r0 i0 lim = imp 0.0 0.0 0 where
        imp r i c =
                if c > lim then
                        0
                else
                        if (r*r)+(i*i) > 4.0 then
                                c
                        else
                                imp
                                        (r*r - i*i + r0)
                                        (2*r*i + i0)
                                        (c+1)

mandelbrotPixel :: Int -> Int -> Int
mandelbrotPixel xi yi =
        let
                r = ((realToFrac xi) / 600.0) * 4.0 - 2.5
                i = ((realToFrac yi) / 600.0) * 4.0 - 2.0
        in
                mandelBrotSample r i 16

swapArgs :: (a -> b -> c) -> (b -> a -> c)
swapArgs f a b = f b a

pnmMaker :: (Int -> Int -> Int) -> Int -> Int -> String
pnmMaker f w h = "P2\n" ++ (show w) ++ " " ++ (show h) ++ "\n16\n" ++
        (concat (intersperse " " (map show (concat (map doLine [1..h]))))) ++ "\n" where
                doLine y = map ((swapArgs f) y) [1..w]

main = writeFile "mandelbrot.pnm" (pnmMaker mandelbrotPixel 600 600)

