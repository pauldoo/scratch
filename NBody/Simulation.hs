module Simulation(initialState, updateState) where
import Data.Maybe(catMaybes)
import State
import NaiveForce
import TreeForce
import Constants

-- forceFn = naiveForceFn
forceFn = treeForceFn

initialState :: State
initialState =
        State ({-- [mainStar] ++ --} (map (makeStar . fromInteger) [1..numStars]) )
    where
        mainStar :: Star
        mainStar = Star {
            location = Vec3 0.0 0.0 0.0,
            velocity = Vec3 0.0 0.0 0.0,
            mass = 1000.0 }
        makeStar :: Double -> Star
        makeStar i = let
                a = i  * goldenAngle
                d = (i / (fromInteger numStars)) * 0.2 + 0.03
            in
                (Star
                    (d `vecMul` (Vec3 (cos a) (sin a) 0.0))
                    (Vec3 0.0 (1.0*(cos a)) 0.0)
                    massOfStar)


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
                velocity = velocity `vecAdd` ((timestep / mass) `vecMul` (forceFunc star))
            }

forget :: Star -> Maybe Star
forget star@(Star { location = (Vec3 x y z) }) =
    if
        (-forgetDistance) <= x && x <= (forgetDistance) &&
        (-forgetDistance) <= y && y <= (forgetDistance) &&
        (-forgetDistance) <= z && z <= (forgetDistance)
    then
        Just star
    else
        Nothing

pairForce :: Blob a => Star -> a -> Vec3
pairForce (Star{ mass = m1, location = p1 }) blob =
    let
        p2 = blobLocation blob
        m2 = blobMass blob
        v = p2 `vecSub` p1
        f = ((bigG * m1 * m2) /  (((vecLength v) + softeningLength) ^2))
    in
        if (p1 == p2) then
            (Vec3 0.0 0.0 0.0)
        else
            f `vecMul` (normalize v)


