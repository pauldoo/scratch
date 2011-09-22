; http://gurmeet.net/puzzles/100-prisoners-and-100-boxes/

(def prisoner-count 100)
(def boxes-per-prisoner 50)
(def trial-count 10000)

(defn simulate []
    (let [
        assignments (shuffle (range prisoner-count))
        ids-in-cycle (fn [n] (rest (iterate assignments n)))
        ]
        (every? (fn [pid] (some #{pid} (take boxes-per-prisoner (ids-in-cycle pid)))) (range prisoner-count))))

(println "Success rate: "
    (/
        (count (filter identity (take trial-count (repeatedly simulate))))
        trial-count))

