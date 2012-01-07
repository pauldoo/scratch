module Main where
    mySplitImp [] a b = (a, b)
    mySplitImp (h:t) a b = (mySplitImp t b (h:a))
    mySplit lst = mySplitImp lst [] []

    myMerge [] [] _ = []
    myMerge lst [] _ = lst
    myMerge [] lst _ = lst
    myMerge a b c =
        let (ha:ta, hb:tb) = (a, b)
        in if (c ha hb)
            then (ha:(myMerge ta b c))
            else (hb:(myMerge a tb c))

    mySort [] _ = []
    mySort [a] _ = [a]
    mySort lst c =
        let (a, b) = (mySplit lst)
        in (myMerge (mySort a c) (mySort b c) c)

    myIndexOf [] _ = 0
    myIndexOf (h:t) a =
        if h == a
            then 0
            else 1 + (myIndexOf t a)

    myStringToNumImp [] k _ = k
    myStringToNumImp (h:t) k f=
        let (v, g) = (myIndexOf ['0'..'9'] h, f || h == '.')
        in
            (if (g && v >= 0 && v <= 9) then 0.1 else 1.0) * (
                myStringToNumImp
                    t
                    (if (v >= 0 && v <= 9) then 10 * k + v else k)
                    g)
    myStringToNum s = myStringToNumImp s 0 False

    myEvery s n = n:(myEvery s (n+s))
    myEveryEigth x y = map (\(a, b) -> a + b) (zip (myEvery 3 x) (myEvery 5 y))

    myScaler s x = s * x
    myAppender c lst = lst ++ [c]

