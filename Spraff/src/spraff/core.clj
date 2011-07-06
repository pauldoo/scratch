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

(def table (ref {}))

(defn split-sentence-to-words [sentence] (re-seq #"\w+" sentence))

(defn select [c i]
    (let [[word weight] (first c)]
        (if (< i weight)
            word
            (recur (rest c) (- i weight)))))
(defn pick-next-word [a b t]
    (let [candidates (t [a b])]
        (select candidates
            (rand-int (reduce + 0 (vals candidates))))))
(defn further-stream-of-shite [a b t]
    (lazy-seq
        (let [c (pick-next-word a b t)]
            (cons c (further-stream-of-shite b c t)))))
(defn stream-of-shite [a b t]
    (concat [a b] (further-stream-of-shite a b t)))
(defn generate-sentence [a b & _]
    (take 10 (stream-of-shite a b @table)))


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
(defn update-table [table message]
    (if (>= (count message) 3)
        (let [[a b c & _] message]
            (recur (update-triple a b c table) (rest message)))
        table))
(defn update-message! [message]
    (dosync (ref-set table (update-table @table (split-sentence-to-words message)))))
(defn log-sentence! [message]
    (with-open [out (writer "log.txt" :append true)]
        (.write out (str message "\n"))
        (update-message! message)))

(defn -main
    "Java entry point.  Counts number of visible frames (via callbacks) and
    terminates the application when none are visible."
    [& args]
    (do
        (dorun (map update-message! (line-seq (reader "log.txt"))))
        (connect
            (create-irc {
                :name "spraffbot"
                :server "localhost"
                :fnmap {
                    :on-message (fn [{:keys [nick channel message irc]}]
                        (if (not (= nick (:name @irc)))
                            (if (.contains message (:name @irc))
                                (send-message irc channel
                                    (apply str (interpose " "
                                        (apply generate-sentence (split-sentence-to-words message)))))
                                (log-sentence! message))))
                }
            })
            :channels ["#sprafftest"])))



