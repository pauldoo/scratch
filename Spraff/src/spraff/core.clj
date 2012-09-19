; :mode=clojure:

;; ## Spraffing IRC bot ##
;; IRC bot which spraffs randomly generated sentences.

;; # Todo list #
;; * Think of next feature..

(ns spraff.core
    (:gen-class)
    (:use
        [clojure.contrib.command-line]
        [clojure.java.io]
        [clojure.xml]
        [clojure.string :only [join]]
        [irclj.core]
        [tuples [core :only [tuple]]]
    )
)

(def corpus-file "corpus.txt")
(def word-pattern #"\S+")
(def prefix-length 3)
(def retries 3)

(defn compare-mixed [a b]
    "Can compare strings with keywords"
    (let [r (compare (keyword? a) (keyword? b))]
        (if (zero? r)
            (compare a b)
            r)))

(defn compare-mixed-seqs [[a & as] [b & bs]]
    "Can compare sequences containing keyword or strings"
    (let [r (compare-mixed a b)]
        (if (and (zero? r) (not (and (empty? as) (empty? bs))))
            (recur as bs)
            r)))

(def empty-state {
    :forwardtable (sorted-map-by compare-mixed-seqs)
    :backwardtable (sorted-map-by compare-mixed-seqs)
})

(defn value-or-default [value default]
    (if (nil? value) default value))

(defn ngram [v]
    {:pre [(= prefix-length (count v))]}
    (let [[a b c] v] (tuple a b c)))

(defn split-sentence-to-words [sentence] (map #(.intern %) (re-seq word-pattern sentence)))

(defn select [c i]
    (let [[word weight] (first c)]
        (if (< i weight)
            word
            (recur (rest c) (- i weight)))))
(defn pick-next-word [prefix t]
    (when-let [candidates (t prefix)] [
        (select candidates
            (rand-int (reduce + 0 (vals candidates))))
        (> (count candidates) 1)
        ]))
(defn further-stream-of-shite [prefix t]
    (lazy-seq
        (when-let [[word had-choice] (pick-next-word prefix t)]
            (cons [word had-choice] (further-stream-of-shite (concat (rest prefix) [word]) t)))))
(defn stream-of-shite [prefix t]
    (concat (map vector prefix (repeat false)) (further-stream-of-shite prefix t)))

(defn sentence-from-seed [seed backward forward]
    (concat
        (drop-last prefix-length
            (reverse (take 500 (stream-of-shite (reverse seed) backward))))
        (take 500 (stream-of-shite seed forward))))

(defn extend-vec [v e n] (vec (concat v (take (- n (count v)) (repeat e)))))
(defn append-null [s] (join (concat (seq s) [(char 0)])))

(defn select-ngrams [forward-table keywords]
    "Uses the forwards-production table as a source of ngrams, and
    filters them to those containing a keyword.  Returns all ngrams if
    filtering would result in an empty result."
    {
        :pre [(sorted? forward-table)]
    }
    (let [filtered-grams
        (apply concat (map
            (fn [k]
                (keys (subseq forward-table
                    >= (extend-vec [k] "" prefix-length)
                    < (extend-vec [(append-null k)] "" prefix-length))))
            keywords))]
        (if (empty? filtered-grams)
            (keys forward-table)
            filtered-grams)))

(defn generate-sentence [state keywords]
    (let [starting-ngrams (seq (select-ngrams (:forwardtable state) keywords))]
        (remove keyword? (map first
            (apply max-key #(count (filter true? (map second %)))
                (take retries (repeatedly #(sentence-from-seed
                    (rand-nth starting-ngrams)
                    (:backwardtable state)
                    (:forwardtable state)))))))))

(defn update-count [m c]
    (assoc m
        c
        (inc (value-or-default (m c) 0))))
(defn update-transition [prefix c t]
    (assoc t
        prefix
        (update-count
            (value-or-default (t prefix) {})
            c)))
(defn update-table [table words]
    (if (> (count words) prefix-length)
        (let [[prefix remainder] (split-at prefix-length words)]
            (recur
                (update-transition
                    (ngram prefix)
                    (first remainder)
                    table)
                (rest words)))
        table))

(defn update-state [message state]
    (let [
        words (concat [:begin] (split-sentence-to-words message) [:end])
        ]
        (assoc state
            :forwardtable
                (update-table (:forwardtable state) words)
            :backwardtable
                (update-table (:backwardtable state) (reverse words))
                )))
(defn log-sentence! [message]
    (with-open [out (writer corpus-file :append true)]
        (.write out (str message "\n"))))
(defn update-state! [message state-ref]
    (dosync (ref-set state-ref
        (update-state message @state-ref))))

; BURRITO!

(def styles [
    "burrito"
    "fajita burrito"
    "burrito bowl"
    "quesadilla"
    "soft tacos"
])

(def fillings [
    "steak"
    "red tractor chicken"
    "carnitas"
    "haggis"
    "vegetarian"
    "basic"
])

(def salsas [
    "pico de gallo"
    "verde"
    "black"
    "roastin' red"
    "extra hot"
])

(def extras [
    "guacamole"
    "sour cream"
    "cheese"
])

(def sides [
    "a single soft taco"
    "tortilla chips"
    "a guacamole pot"
    "a salsa pot"
    "a small tortilla"
    "a bean pot"
    "a sour cream pot"
    "a jalapeno pot"
])

(defn rand-bool [] (= (rand-int 2) 1))

(defn single [coll] (rand-nth coll))
(defn multiple [coll] (filter (fn [_] (rand-bool)) coll))
(defn maybe-single [coll s] (let [c (rand-nth (cons nil coll))]
    (if (nil? c)
        []
        [(str c s)])))
(defn inject-and [coll] (concat (take (dec (count coll)) coll) [(str "and " (last coll))]))

(defn generate-burrito [] (str
    (single fillings) " "
    (single styles) " with "
    (join ", " (inject-and (concat
        [(str (single salsas) " salsa")]
        (multiple extras)
        (maybe-single sides " on the side"))))
    ", please!"
))

; End BURRITO!

; YOUTUBE!

(defn elements-with-tag [tag x]
    (cond
        (map? x)
            (if (= (:tag x) tag)
                [x]
                (elements-with-tag tag (:content x)))
        (coll? x)
            (apply concat (map (partial elements-with-tag tag) x))
        :else
            []
            ))

(defn get-all-comment-urls [x]
    (apply concat (map
        (fn [k]
            (map
                (fn [e] (:href (:attrs e)))
                (filter
                    (fn [e] (= :gd:feedLink (:tag e)))
                    (:content k))))
        (elements-with-tag :gd:comments x))))


(defn get-all-comments [x]
    (apply concat (map
        (fn [k] (:content k))
        (elements-with-tag :content x))))

; Could use something other than the most commented video list:
; https://developers.google.com/youtube/2.0/reference#Standard_feeds
(defn pick-random-youtube-comment []
    (let [
       feed-xml (clojure.xml/parse "https://gdata.youtube.com/feeds/api/standardfeeds/most_discussed")
       comments-url (rand-nth (get-all-comment-urls feed-xml))
       comments-xml (clojure.xml/parse comments-url)
       comment (rand-nth (get-all-comments comments-xml))]
       comment))

; End YOUTUBE!

(defn memory-stat []
    (let [
        r (Runtime/getRuntime)
        total (.totalMemory r)
        free (.freeMemory r)
        used (- total free)
        to-mb (fn [b] (Math/round (double (/ b (* 1024 1024)))))
        ]
        (str
            "Total: " (to-mb total) " MiB" " - "
            "Used: " (to-mb used) " MiB" " - "
            "Free: " (to-mb free) " MiB")))

(defn on-message [{:keys [nick channel message irc]} state-ref]
    (log-sentence! message)
    (update-state! message state-ref)
    (if (and (not (= nick (:name @irc))) (.contains message (:name @irc)))
        (send-message irc channel
            (join (take 450 (join " " (generate-sentence
                @state-ref
                (set (remove #(.contains % (:name @irc)) (split-sentence-to-words message)))))))))
    (if (= message "\\o/")
        (send-message irc channel "\\ӧ/"))
    (if (.startsWith message "!burrito")
        (send-message irc channel
            (generate-burrito)))
    (if (.startsWith message "!comment")
        (send-message irc channel
            (pick-random-youtube-comment)))
    (if (.startsWith message "!memory")
        (send-message irc channel
            (memory-stat)))
)

(defn -main
    [& args]
    (with-command-line
        args
        "Arguments: -channel #mychannel -nick botnick -server irc.server.com"
        [
            [channel c "IRC Channel to join." "#sprafftest"]
            [nick n "Bot's IRC nick." "spraffbot"]
            [server s "IRC server address." "localhost"]
        ]
        (let [state-ref (ref empty-state)] (do
            (println channel nick server)
            (connect
                (create-irc {
                    :name nick
                    :server server
                    :fnmap {
                        :on-message #(on-message % state-ref)
                    }
                })
                :channels [channel])
            (time (with-open [in (reader corpus-file)]
                (dorun (map #(update-state! % state-ref) (line-seq in)))))
            (println (memory-stat))
))))



