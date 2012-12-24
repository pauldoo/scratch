module State where

data Vec3 = Vec3 Double Double Double
    deriving (Show, Eq);

data Axis = X | Y | Z;

data Star = Star {
        location :: Vec3,
        velocity :: Vec3,
        mass :: Double
    } deriving (Show)

data State = State {
        stars :: [Star]
    } deriving (Show)

class Blob a where
    blobMass :: a -> Double
    blobLocation :: a -> Vec3

vecAdd :: Vec3 -> Vec3 -> Vec3
(Vec3 a b c) `vecAdd` (Vec3 d e f) = (Vec3 (a+d) (b+e) (c+f))

vecMul :: Double -> Vec3 -> Vec3
x `vecMul` (Vec3 a b c) = (Vec3 (x*a) (x*b) (x*c))

vecSub :: Vec3 -> Vec3 -> Vec3
a `vecSub` b = (a `vecAdd` ((-1.0) `vecMul` b))

vecLength :: Vec3 -> Double
vecLength (Vec3 a b c) = sqrt (a*a + b*b + c*c)

normalize :: Vec3 -> Vec3
normalize v = (1.0 / (vecLength v)) `vecMul` v

vecMap2 :: (Double -> Double -> Double) -> Vec3 -> Vec3 -> Vec3
vecMap2 fn (Vec3 a b c) (Vec3 d e f) = Vec3 (fn a d) (fn b e) (fn c f)

vecElem :: Axis -> Vec3 -> Double
vecElem X (Vec3 x _ _) = x
vecElem Y (Vec3 _ y _) = y
vecElem Z (Vec3 _ _ z) = z

instance Blob Star where
    blobMass = mass
    blobLocation = location

