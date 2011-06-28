; :mode=clojure:

;(def input-corpus (slurp "input.txt"))

(def sentence-delimiter-pattern #"\.\s")

(def word-extractor-pattern #"\w+")

(defn split
    [s re]
    (vec (.split re s)))

(map (partial re-seq word-extractor-pattern) (split "This is a test. This is another." sentence-delimiter-pattern))


