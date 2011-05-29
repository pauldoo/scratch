; :mode=clojure:

(def zero (fn [f] (fn [x] x)))

(defn add-1 [n]
    (fn [f] (fn [x] (f ((n f) x)))))



