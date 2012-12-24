module Simulation(initialState, updateState) where
import State
import NaiveForce
import TreeForce
import Data.Maybe(catMaybes)

-- forceFn = naiveForceFn
forceFn = treeForceFn

clampForce :: Vec3 -> Vec3
clampForce v = ((vecLength v) `min` 10.0) `vecMul` (normalize v)

initialState :: State
initialState =
        State (map makeStar [1..numStars])
    where
        numStars = 1000
        mainStar = (Star {
                location = (Vec3 0.0 0.0 0.0),
                velocity = (Vec3 0.0 0.0 0.0),
                mass = 100.0})
        makeStar :: Double -> Star
        makeStar i = let
                a = i  * goldenAngle
                d = i / numStars
            in
                (Star
                    (d `vecMul` (Vec3 (cos a) (sin a) 0.0))
                    ((d * 0.7) `vecMul` (Vec3 (sin a) (-(cos a)) 0.0))
                    1.0)

goldenAngle :: Double
goldenAngle = pi * (3.0 - (sqrt 5.0))

updateState :: Double -> State -> State
updateState timestep state@(State { stars = starList }) =
    state { stars = catMaybes (map (updateStar timestep starList) starList) }

updateStar :: Double -> [Star] -> Star -> Maybe Star
updateStar timestep otherstars =
    let
        forceFunc = (forceFn pairForce otherstars) :: Star -> Vec3
    in
        \star@(Star { location = location, velocity = velocity, mass = mass}) ->
            forget $ star {
                location = location `vecAdd` (timestep `vecMul` velocity),
                velocity = velocity `vecAdd` ((timestep / mass) `vecMul` (clampForce (forceFunc star)))
            }

forget :: Star -> Maybe Star
forget star@(Star { location = (Vec3 x y z) }) =
    if
        (-10.0) <= x && x <= (10.0) &&
        (-10.0) <= y && y <= (10.0) &&
        (-10.0) <= z && z <= (10.0)
    then
        Just star
    else
        Nothing


bounce :: Star -> Star
bounce star@(Star { location = (Vec3 x y z), velocity = (Vec3 a b c) }) =
    star {
        location =
            (Vec3
                (if (-1.0) <= x && x <= (1.0) then x else ((-1.0) `max` x `min` (1.0)))
                (if (-1.0) <= y && y <= (1.0) then y else ((-1.0) `max` y `min` (1.0)))
                (if (-1.0) <= z && z <= (1.0) then z else ((-1.0) `max` z `min` (1.0)))),
        velocity =
            (Vec3
                (if (-1.0) <= x && x <= (1.0) then a else (-a))
                (if (-1.0) <= y && y <= (1.0) then b else (-b))
                (if (-1.0) <= z && z <= (1.0) then c else (-c)))
    }

pairForce :: Blob a => Star -> a -> Vec3
pairForce (Star{ mass = m1, location = p1 }) blob =
    let
        p2 = blobLocation blob
        m2 = blobMass blob
        v = p2 `vecSub` p1
        f = ((bigG * m1 * m2) /  ((vecLength v)^2))
    in
        if (p1 == p2) then
            (Vec3 0.0 0.0 0.0)
        else
            f `vecMul` (normalize v)

bigG :: Double
bigG = 0.001

