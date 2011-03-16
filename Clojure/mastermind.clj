; :mode=clojure:

; Plays the mastermind board game
; http://en.wikipedia.org/w/index.php?title=Mastermind_(board_game)&oldid=416560743

(defn third [s]
    (first (rest (rest s))))

(defn score-blacks
    [answer guess]
    (assert (= (count answer) (count guess)))
    (if (empty? answer)
        [0 [] []]
        (let [
            is-correct (= (first answer) (first guess))
            remainder (score-blacks (rest answer) (rest guess))]
            [
                (+
                    (if is-correct 1 0)
                    (first remainder))
                (if is-correct
                    (second remainder)
                    (cons (first answer) (second remainder)))
                (if is-correct
                    (third remainder)
                    (cons (first guess) (third remainder)))
            ])))

(defn remove-first
    [coll v]
    (concat
        (rest (filter (fn [e] (= e v)) coll))
        (filter (fn [e] (not (= e v))) coll)))

(defn score-whites
    [answer guess]
    (if (empty? guess)
        0
        (let [is-correct (contains? (set answer) (first guess))]
            (if is-correct
                (+ 1 (score-whites (remove-first answer (first guess)) (rest guess)))
                (score-whites answer (rest guess))))))

(defn score
    [answer guess]
    (assert (= (count answer) (count guess)))
    (let [[blacks ra rg] (score-blacks answer guess)]
        {
            :blacks blacks
            :whites (score-whites ra rg)
        }))

(defn weighted-average
    [& values]
    (/
        (reduce + (map #(* % %) values))
        (reduce + values)))

(defn rate-guess
    [possible-codes guess rate-fn]
    (apply rate-fn (map second
        (frequencies
        (map (fn [code] (score code guess)) possible-codes)))))

(defn gen-all-codes
    [colours length]
    (if (= length 1)
        (map vector colours)
        (for [h colours t (gen-all-codes colours (dec length))] (cons h t))))

(defn next-guess
    [all-codes possible-codes rate-fn]
    (assert (not (empty? possible-codes)))
    (if (= 1 (count possible-codes))
        [(first possible-codes) 0]
        (reduce
            (fn [a b] (if (< (second a) (second b)) a b))
            (pmap (juxt identity (fn [g] (rate-guess possible-codes g rate-fn))) all-codes))))

; ----------------------


(def default-codes (gen-all-codes [:a :b :c :d :e :f] 4))

(defn play-game
    ([solution-code]
        [
            (play-game default-codes default-codes solution-code max)
            (play-game default-codes default-codes solution-code weighted-average)
        ])

    ([all-codes possible-codes solution-code rate-fn]
        (assert (not (empty? possible-codes)))
        (assert (contains? (set possible-codes) solution-code))
        (let [
            [my-guess rating] (next-guess all-codes possible-codes rate-fn)
            my-guess-score (score solution-code my-guess)
            ]
            (cons [(count possible-codes) my-guess my-guess-score (float rating)]
                (if (= my-guess solution-code)
                    [my-guess]
                    (play-game
                        all-codes
                        (filter (fn [p] (= (score p my-guess) my-guess-score)) possible-codes)
                        solution-code
                        rate-fn))))))

(try
    (println (doall (map println (reduce concat (play-game [:a :b :c :d])))))
    (println (doall (map println (reduce concat (play-game [:c :c :c :c])))))
    (println (doall (map println (reduce concat (play-game [:f :a :f :d])))))
    (catch Exception e (.printStackTrace e)))

