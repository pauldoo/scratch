; :mode=clojure:

(def zero (fn [f] (fn [x] x)))

(defn add-1 [n]
    (fn [f] (fn [x] (f ((n f) x)))))

(defn one [f] (fn [x] (f x)))

(defn two [f] (fn [x] (f (f x))))

(println
    (((add-1 (add-1 (add-1 (add-1 zero)))) inc) 0)
    ((one inc) 0)
    ((two inc) 0))

