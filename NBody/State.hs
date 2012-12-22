module State where

data Vec3 = Vec3 Double Double Double
    deriving (Show, Eq);

data Star = Star {
        location :: Vec3,
        velocity :: Vec3,
        mass :: Double
    } deriving (Show)

data State = State {
        stars :: [Star]
    } deriving (Show)

vecAdd :: Vec3 -> Vec3 -> Vec3
(Vec3 a b c) `vecAdd` (Vec3 d e f) = (Vec3 (a+d) (b+e) (c+f))

vecMul :: Double -> Vec3 -> Vec3
x `vecMul` (Vec3 a b c) = (Vec3 (x*a) (x*b) (x*c))

vecSub :: Vec3 -> Vec3 -> Vec3
a `vecSub` b = (a `vecAdd` ((-1.0) `vecMul` b))

vecLength :: Vec3 -> Double
vecLength (Vec3 a b c) = sqrt (a*a + b*b + c*c)


