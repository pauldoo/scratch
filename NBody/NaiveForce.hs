module NaiveForce(naiveForceFn) where
import State

naiveForceFn :: (Star -> Star -> Vec3) -> [Star] -> Star -> Vec3
naiveForceFn pairForce otherstars star =
    foldl vecAdd (Vec3 0.0 0.0 0.0) (map (pairForce star) otherstars)


