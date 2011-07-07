; :mode=clojure:

;; ## Spraffing IRC bot ##
;; IRC bot which spraffs randomly generated sentences.

;; # Todo list #
;; * Think of next feature..

(ns spraff.core
    (:gen-class)
    (:use
        [clojure.java.io]
        [irclj.core])
)

(def irc-server "localhost")
(def bot-nick "spraffbot")
(def irc-channels ["#sprafftest"])
(def corpus-file "corpus.txt")
(def word-pattern #"\S+")
(def generated-sentence-length 10)
(def prefix-length 2)

(def table (ref {}))
(def starters (ref []))

(defn split-sentence-to-words [sentence] (re-seq word-pattern sentence))

(defn select [c i]
    (let [[word weight] (first c)]
        (if (< i weight)
            word
            (recur (rest c) (- i weight)))))
(defn pick-next-word [prefix t]
    (when-let [candidates (t prefix)]
        (select candidates
            (rand-int (reduce + 0 (vals candidates))))))
(defn further-stream-of-shite [prefix t]
    (lazy-seq
        (when-let [c (pick-next-word prefix t)]
            (cons c (further-stream-of-shite (concat (rest prefix) [c]) t)))))
(defn stream-of-shite [prefix t]
    (concat prefix (further-stream-of-shite prefix t)))
(defn generate-sentence []
    (take generated-sentence-length (stream-of-shite
        (nth @starters (rand-int (count @starters)))
        @table)))

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
        (let [[prefix [c & _]] (split-at prefix-length words)]
            (recur (update-transition prefix c table) (rest words)))
        table))
(defn update-message! [message]
    (dosync
        (let [words (split-sentence-to-words message)]
            (ref-set table (update-table @table words))
            (if (>= (count words) prefix-length)
                (ref-set starters (conj @starters (take prefix-length words)))))))
(defn log-sentence! [message]
    (with-open [out (writer corpus-file :append true)]
        (.write out (str message "\n"))
        (update-message! message)))

(defn -main
    "Java entry point.  Counts number of visible frames (via callbacks) and
    terminates the application when none are visible."
    [& args]
    (do
        (dorun (map update-message! (line-seq (reader corpus-file))))
        (connect
            (create-irc {
                :name bot-nick
                :server irc-server
                :fnmap {
                    :on-message (fn [{:keys [nick channel message irc]}]
                        (if (not (= nick (:name @irc)))
                            (if (.contains message (:name @irc))
                                (send-message irc channel
                                    (apply str (interpose " " (generate-sentence))))
                                (log-sentence! message))))
                }
            })
            :channels irc-channels)))



