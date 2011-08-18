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
        [clojure.string :only [join]]
        [irclj.core]
    )
)

(def corpus-file "corpus.txt")
(def word-pattern #"\S+")
(def prefix-length 3)
(def retries 3)

(def empty-state {
    :table {}
    :starter []
})

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
(defn generate-sentence [state keywords]
    (let [{:keys [table starters]} state]
        (if (empty? keywords)
            (drop-last (map first
                (apply max-key #(count (filter true? (map second %)))
                    (take retries (repeatedly #(take 1000 (stream-of-shite
                        (nth starters (rand-int (count starters)))
                        table)))))))
            (let [filtered-starters
                    (filter
                        (fn [s] (not (empty? (filter
                            (fn [k] (not (empty? (filter #(.contains % k) s))))
                            keywords))))
                        starters)]
                (if (empty? filtered-starters)
                    (generate-sentence state [])
                    (generate-sentence
                        (assoc state :starters filtered-starters)
                        []))))))

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
                    prefix
                    (first remainder)
                    table)
                (rest words)))
        table))
(defn update-state [message state]
    (let [words (split-sentence-to-words message)]
        (assoc state
            :table
                (update-table (:table state) (concat [:begin] words [:end]))
            :starters
                (if (>= (count words) prefix-length)
                    (conj (:starters state) (take prefix-length words))
                    (:starters state)))))
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

; BURRITO!

(defn on-message [{:keys [nick channel message irc]} state-ref]
    (log-sentence! message)
    (update-state! message state-ref)
    (if (and (not (= nick (:name @irc))) (.contains message (:name @irc)))
        (send-message irc channel
            (join " " (generate-sentence
                @state-ref
                (let [keywords (set (filter #(not (.contains % (:name @irc))) (split-sentence-to-words message)))]
                    (if (<= (count keywords) 3) keywords #{}))))))
    (if (.startsWith message "!burrito")
        (send-message irc channel
            (generate-burrito)))
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
                        :on-message (fn [event] (on-message event state-ref))
                    }
                })
                :channels [channel])
            (with-open [in (reader corpus-file)]
                (dorun (map #(update-state! % state-ref) (line-seq in))))))))



