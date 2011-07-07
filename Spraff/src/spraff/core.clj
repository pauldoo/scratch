; :mode=clojure:

;; ## Spraffing IRC bot ##
;; IRC bot which spraffs randomly generated sentences.

;; # Todo list #
;; * Log all messages.
;; * Update markov tables based on channel chat.
;; * Rehydrate tables from old log files on startup.

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

(def table (ref {}))
(def starters (ref []))

(defn split-sentence-to-words [sentence] (re-seq word-pattern sentence))

(defn select [c i]
    (let [[word weight] (first c)]
        (if (< i weight)
            word
            (recur (rest c) (- i weight)))))
(defn pick-next-word [a b t]
    (when-let [candidates (t [a b])]
        (select candidates
            (rand-int (reduce + 0 (vals candidates))))))
(defn further-stream-of-shite [a b t]
    (lazy-seq
        (when-let [c (pick-next-word a b t)]
            (cons c (further-stream-of-shite b c t)))))
(defn stream-of-shite [a b t]
    (concat [a b] (further-stream-of-shite a b t)))
(defn generate-sentence []
    (let [[a b] (nth @starters (rand-int (count @starters)))]
        (take generated-sentence-length (stream-of-shite a b @table))))

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
(defn update-table [table words]
    (if (>= (count words) 3)
        (let [[a b c & _] words]
            (recur (update-triple a b c table) (rest words)))
        table))
(defn update-message! [message]
    (dosync
        (let [words (split-sentence-to-words message)]
            (ref-set table (update-table @table words))
            (if (>= (count words) 2)
                (ref-set starters (conj @starters (take 2 words)))))))
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



