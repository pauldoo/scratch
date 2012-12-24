module TreeForce(treeForceFn) where
import Data.List(sortBy)
import State
import Debug.Trace(traceShow)

treeForceFn :: (Star -> StarTree -> Vec3) -> [Star] -> Star -> Vec3
treeForceFn pairForce otherstars =
    let
        startree = buildTree otherstars
    in
        \star ->
            let
                nodes = (relevantNodes startree)
                relevantNodes :: StarTree -> [StarTree]
                relevantNodes leaf@(Leaf _) = [leaf]
                relevantNodes node@(Node{}) =
                    if
                        (bounds node) `vecContains` (location star) ||
                        (vecLength (pairForce star node) * (diameter node)) /
                            ((mass star) * (distance star node)) >= 0.1
                    then
                        concatMap relevantNodes [(leafA node), (leafB node)]
                    else
                        [node]
            in
                -- traceShow (length nodes)
                    (foldl vecAdd (Vec3 0.0 0.0 0.0) (map (pairForce star) nodes))

diameter :: StarTree -> Double
diameter n = vecLength ((mins n) `vecSub` (maxs n))

distance :: Star -> StarTree -> Double
distance s st = vecLength ((location s) `vecSub` (blobLocation st))

bounds :: StarTree -> (Vec3, Vec3)
bounds (Leaf (Star{ location = loc })) = (loc, loc)
bounds (Node {mins = mins, maxs = maxs}) = (mins, maxs)

vecContains :: (Vec3, Vec3) -> Vec3 -> Bool
vecContains ((Vec3 a b c), (Vec3 d e f)) (Vec3 x y z) =
    a <= x && x <= d &&
        b <= y && y <= e &&
            c <= z && z <= f

buildTree :: [Star] -> StarTree
buildTree [] = undefined
buildTree [s] = Leaf s
buildTree list = let
        locations = map location list
        mins = findMins locations
        maxs = findMaxs locations
        splitAxis = determineSplit mins maxs
        (leafListA, leafListB) = splitInMiddle (sortBy (axisCompare splitAxis) list)
        leafA = buildTree leafListA
        leafB = buildTree leafListB
        (meanLocation, totalMass) = summarizeStars list
    in
        Node {
            mins = mins,
            maxs = maxs,
            meanLocation = meanLocation,
            totalMass = totalMass,
            leafA = leafA,
            leafB = leafB
        }

summarizeStars :: [Star] -> (Vec3, Double)
summarizeStars lst =
        imp lst (Vec3 0.0 0.0 0.0) 0.0
    where
        imp :: [Star] -> Vec3 -> Double -> (Vec3, Double)
        imp [] p m = ((1.0 / m) `vecMul` p, m)
        imp (Star{ location = xloc, mass = xmass }:xs) p m =
            imp xs (p `vecAdd` (xmass `vecMul` xloc)) (m + xmass)

splitInMiddle :: [a] -> ([a], [a])
splitInMiddle lst =
        splitAt (n `div` 2) lst
    where n = length lst

axisCompare :: Axis -> Star -> Star -> Ordering
axisCompare axis a b =
        compare (getElem a) (getElem b)
    where
        getElem :: Star -> Double
        getElem = (vecElem axis) . location

determineSplit :: Vec3 -> Vec3 -> Axis
determineSplit mins maxs =
    let
        (Vec3 x y z) = maxs `vecSub` mins
    in
        if x >= y && x >= z then
            X
        else
            if y >= z  then Y else Z

findMins :: [Vec3] -> Vec3
findMins lst@(v:_) = foldl (vecMap2 min) v lst

findMaxs :: [Vec3] -> Vec3
findMaxs lst@(v:_) = foldl (vecMap2 max) v lst

data StarTree =
    Leaf Star |
    Node {
        mins :: Vec3,
        maxs :: Vec3,
        meanLocation :: Vec3,
        totalMass :: Double,
        leafA :: StarTree,
        leafB :: StarTree
    } deriving (Show)

instance Blob StarTree where
    blobMass (Leaf leaf) = blobMass leaf
    blobMass node@(Node{}) = totalMass node
    blobLocation (Leaf leaf) = blobLocation leaf
    blobLocation node@(Node{}) = meanLocation node


