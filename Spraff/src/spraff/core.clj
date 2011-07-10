; :mode=clojure:

;; ## Spraffing IRC bot ##
;; IRC bot which spraffs randomly generated sentences.

;; # Todo list #
;; * Think of next feature..

(ns spraff.core
    (:gen-class)
    (:use
        [clojure.java.io]
        [clojure.string :only [join]]
        [irclj.core])
)

(def irc-server "localhost")
(def bot-nick "spraffbot")
(def irc-channels ["#sprafftest"])
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
(defn generate-sentence [{:keys [table starters]}]
    (drop-last (map first
        (apply max-key #(count (filter true? (map second %)))
            (take retries (repeatedly #(stream-of-shite
                    (nth starters (rand-int (count starters)))
                    table)))))))

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
    (if (>= (count words) prefix-length)
        (let [[prefix remainder] (split-at prefix-length words)]
            (recur
                (update-transition
                    prefix
                    (if (empty? remainder) :end (first remainder))
                    table)
                (rest words)))
        table))
(defn update-state [message state]
    (let [words (split-sentence-to-words message)]
        (assoc state
            :table
                (update-table (:table state) words)
            :starters
                (concat (:starters state)
                    (if (>= (count words) prefix-length)
                        [(take prefix-length words)]
                        [])))))
(defn log-sentence! [message]
    (with-open [out (writer corpus-file :append true)]
        (.write out (str message "\n"))))
(defn update-state! [message state-ref]
    (dosync (ref-set state-ref
        (update-state message @state-ref))))

(defn on-message [{:keys [nick channel message irc]} state-ref]
    (log-sentence! message)
    (update-state! message state-ref)
    (if (and (not (= nick (:name @irc))) (.contains message (:name @irc)))
        (send-message irc channel
            (join " " (generate-sentence @state-ref)))))

(defn -main
    [& args]
    (let [state-ref (ref empty-state)] (do
        (connect
            (create-irc {
                :name bot-nick
                :server irc-server
                :fnmap {
                    :on-message (fn [event] (on-message event state-ref))
                }
            })
            :channels irc-channels)
        (dorun (map
            #(update-state! % state-ref)
            (line-seq (reader corpus-file)))))))



