; :mode=clojure:

;(def input-corpus (slurp "input.txt"))

(def sentence-delimiter-pattern #"\.\s")

(def word-extractor-pattern #"\w+")

(defn split
    [s re]
    (vec (.split re s)))

(defn extract
    [s re]
    (let [
        m (.matcher re s)
        f (fn [r] (if (.find m) (recur (conj r (.group m))) r))
        ]
    (f [])))

(map #(extract % word-extractor-pattern) (split "This is a test. This is another." sentence-delimiter-pattern))


