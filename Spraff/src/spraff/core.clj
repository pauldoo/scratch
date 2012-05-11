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
    )
)

(def corpus-file "corpus.txt")
(def word-pattern #"\S+")
(def prefix-length 3)
(def retries 3)

(def empty-state {
    :forwardtable {}
    :backwardtable {}
    :n-grams (sorted-set)
})


(defn make-canonicalizer [] {
    :whm (new java.util.WeakHashMap)
    :lock (new java.lang.Object)})

(defn canonical-get [c v]
    {
        :pre [v]
        :post [% (= v %)]
    }
    (let [^java.util.WeakHashMap whm (:whm c)]
        (locking (:lock c)
            (if-let [result
                (if-let [^java.lang.ref.WeakReference wr (.get whm v)]
                    (if-let [ret (.get wr)]
                        ret))]
                result
                (do
                    (.put whm v (new java.lang.ref.WeakReference v))
                    (canonical-get c v) )))))

(def canon (let [c (make-canonicalizer)]
    (fn [v] (canonical-get c v))))

(defn split-sentence-to-words [sentence] (map canon (re-seq word-pattern sentence)))

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

(defn generate-sentence [state keywords]
    {
        :pre [(or (empty? keywords) (sorted? (:n-grams state)))]
    }
    (if (empty? keywords)
        (remove keyword? (map first
            (apply max-key #(count (filter true? (map second %)))
                (take retries (repeatedly #(sentence-from-seed
                    (rand-nth (seq (:n-grams state)))
                    (:backwardtable state)
                    (:forwardtable state)))))))
        (let [filtered-grams
                (apply concat
                    (map
                        (fn [k]
                            (subseq (:n-grams state)
                                >= (extend-vec [k] "" prefix-length)
                                < (extend-vec [(append-null k)] "" prefix-length)))
                        keywords))]
            (if (empty? filtered-grams)
                (generate-sentence state [])
                (generate-sentence
                    (assoc state :n-grams filtered-grams)
                    [])))))

(defn value-or-default [value default]
    (if (nil? value) default value))
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
                    (canon (vec prefix))
                    (first remainder)
                    table)
                (rest words)))
        table))
(defn update-ngrams [ngrams words]
    (if (>= (count words) prefix-length)
        (let [[prefix remainder] (split-at prefix-length words)]
            (recur
                (conj ngrams (canon (vec prefix)))
                (rest words)))
        ngrams))

(defn update-state [message state]
    (let [words (concat [:begin] (split-sentence-to-words message) [:end])]
        (assoc state
            :forwardtable
                (update-table (:forwardtable state) words)
            :backwardtable
                (update-table (:backwardtable state) (reverse words))
            :n-grams
                (update-ngrams (:n-grams state) (remove keyword? words)))))
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

(defn on-message [{:keys [nick channel message irc]} state-ref]
    (log-sentence! message)
    (update-state! message state-ref)
    (if (and (not (= nick (:name @irc))) (.contains message (:name @irc)))
        (send-message irc channel
            (join (take 450 (join " " (generate-sentence
                @state-ref
                (set (remove #(.contains % (:name @irc)) (split-sentence-to-words message)))))))))
    (if (.startsWith message "!burrito")
        (send-message irc channel
            (generate-burrito)))
    (if (.startsWith message "!comment")
        (send-message irc channel
            (pick-random-youtube-comment)))
    (if (.startsWith message "!memory")
        (send-message irc channel
            (let [
                r (Runtime/getRuntime)
                free (.freeMemory r)
                total (.totalMemory r)
                to-mb (fn [b] (Math/round (double (/ b (* 1024 1024)))))
                ]
                (str "Total: " (to-mb total) " MiB" " - " "Free: " (to-mb free) " MiB"))))
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
                (dorun (map #(update-state! % state-ref) (line-seq in)))))))))



