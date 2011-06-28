; :mode=clojure:

(def word-extractor-pattern #"\w+")

(def input-corpus (re-seq word-extractor-pattern (slurp "input.txt")))

(defn value-or-default [value default]
    (if (nil? value) default value))

(defn update-count [m c]
    (assoc m
        c
        (inc (value-or-default (m c) 0))))

(defn update-triple [a b c t]
    (assoc t
        [a b]
        (update-count
            (value-or-default (t [a b]) {})
            c)))

(defn create-table [whole-corpus]
    (loop [t {} corpus whole-corpus]
        (if (>= (count corpus) 3)
            (let [[a b c & _] corpus]
                (recur (update-triple a b c t) (rest corpus)))
            t)))

(defn select [c i]
    (let [[word weight] (first c)]
        (if (< i weight)
            word
            (recur (rest c) (- i weight)))))

(defn pick-next-word [a b t]
    (let [candidates (t [a b])]
        (select candidates
            (rand-int (reduce + 0 (vals candidates))))))

(def table (create-table input-corpus))

(defn further-stream-of-shite [a b t]
    (lazy-seq
        (let [c (pick-next-word a b t)]
            (cons c (further-stream-of-shite b c t)))))

(defn stream-of-shite [a b t]
    (concat [a b] (further-stream-of-shite a b t)))

(println (take 100 (stream-of-shite (first input-corpus) (second input-corpus) table)))

