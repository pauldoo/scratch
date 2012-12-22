module Simulation(initialState, updateState) where
import State

initialState :: State
initialState =
        State (map makeStar [1..100])
    where
        makeStar :: Double -> Star
        makeStar i = let
                a = (i / 100.0) * 2.0 * pi
                d = 0.2 * ((sin i) + 2.0)
            in
                (Star
                    (d `vecMul` (Vec3 (cos a) (sin a) 0.0))
                    ((0.1/d) `vecMul` (Vec3 (sin a) (-(cos a)) 0.0))
                    1.0)

updateState :: Double -> State -> State
updateState timestep state@(State { stars = starList }) =
    state { stars = map (updateStar timestep starList) starList }

updateStar :: Double -> [Star] -> Star -> Star
updateStar timestep otherstars star@(Star { location = location, velocity = velocity, mass = mass}) =
        star {
            location = location `vecAdd` (timestep `vecMul` velocity),
            velocity = velocity `vecAdd` ((timestep / mass) `vecMul` (forceFunc star))
        }
    where
        forceFunc = (forceForStar otherstars) :: Star -> Vec3

forceForStar :: [Star] -> Star -> Vec3
forceForStar otherstars s =
    foldl vecAdd (Vec3 0.0 0.0 0.0) (map (pairForce s) otherstars)

pairForce :: Star -> Star -> Vec3
pairForce (Star{ mass = m1, location = p1 }) (Star{ mass = m2, location = p2}) =
    if (p1 == p2) then
        (Vec3 0.0 0.0 0.0)
    else
            ((bigG * m1 * m2) /  ((vecLength v)^^3))`vecMul` v
        where v = (p2 `vecSub` p1)

bigG :: Double
bigG = 0.001

