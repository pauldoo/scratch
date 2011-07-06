; :mode=clojure:

;; ## Spraffing IRC bot ##
;; IRC bot which spraffs randomly generated sentences.

;; # Todo list #
;; * Log all messages.
;; * Update markov tables based on channel chat.
;; * Rehydrate tables from old log files on startup.

(ns spraff.core
    (:gen-class)
    (:use [irclj [core]])
)

(defn -main
    "Java entry point.  Counts number of visible frames (via callbacks) and
    terminates the application when none are visible."
    [& args]
    (let [bot
        (connect
            (create-irc {
                :name "spraffbot"
                :server "localhost"
                :fnmap {
                }
            })
            :channels ["#sprafftest"])]
        nil)
)



