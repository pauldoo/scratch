; :mode=clojure:

;; ## Wolfram Alpha IRC bot ##
;; IRC bot which can query Wolfram Alpha.

;; # Todo list #
;; * Think of next feature..

(ns alphabot.core
    (:gen-class)
    (:use
        [clojure.contrib.command-line]
        [clojure.xml]
        [irclj.core]
    )
)

; WOLFRAM

(defn url-encode [s]
    (java.net.URLEncoder/encode s))

(defn get-all-plaintext-lines [o]
    (if (map? o)
        (mapcat get-all-plaintext-lines (:content o))
        (re-seq #".+" o)))

(defn extract-nested-errors [xmldoc parent-tag child-tag]
    (filter
        (fn [t] (= (:tag t) child-tag))
        (mapcat
            (fn [t] (:content t))
            (filter
                (fn [t] (= (:tag t) parent-tag))
                (:content xmldoc)))))

(defn extract-didyoumeans [xmldoc]
    (mapcat
        (fn [t] (let [[l & ls] (:content t)]
            (concat
                [(str "Did you mean: " l)]
                ls)))
        (extract-nested-errors xmldoc :didyoumeans :didyoumean)))

(defn extract-tips [xmldoc]
    (map
        (fn [t] (str "Tip: " (:text (:attrs t))))
        (extract-nested-errors xmldoc :tips :tip)))

(defn extract-top-level-errors [xmldoc parent-tag attrs]
    (mapcat
        (fn [t]
            (map (fn [a] (a (:attrs t))) attrs))
        (filter (fn [t] (= (:tag t) parent-tag))
            (:content xmldoc))))

(defn extract-languagemsg [xmldoc]
    (extract-top-level-errors xmldoc :languagemsg [:other :english]))

(defn extract-futuretopic [xmldoc]
    (extract-top-level-errors xmldoc :futuretopic [:topic :msg]))

(defn get-results [xmldoc query]
    (if (= (:success (:attrs xmldoc)) "true")
        (mapcat
            (fn [t]
                (let [[l & ls] (get-all-plaintext-lines t)]
                    (concat
                        [(str (:title (:attrs t)) ": " l)]
                        ls)))
            (take 2
                (filter
                    (fn [t] (and
                            (= (:tag t) :pod)
                            (not (empty? (get-all-plaintext-lines t)))
                            ))
                    (:content xmldoc))))
        (concat
            [(str "Something went wrong, try the website for a better debug: http://www.wolframalpha.com/input/?i=" (url-encode query))]
            (extract-languagemsg xmldoc)
            (extract-futuretopic xmldoc)
            (extract-tips xmldoc)
            (extract-didyoumeans xmldoc)
)))

(defn make-query-url [query appid]
    (str "http://api.wolframalpha.com/v2/query?input=" (url-encode query) "&appid=" (url-encode appid)))

(defn ask-wolfram [query appid]
    (let [
        query-url (make-query-url query appid)
        response-xml (clojure.xml/parse query-url)
        result (get-results response-xml query)
        ]
        (println query)
        (println query-url)
        result))

; End WOLFRAM

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

(def alpha-command "!alpha ")
(defn on-message [{:keys [nick channel message irc]} wolfram-appid]
    (if (.startsWith message alpha-command)
        (let [results (ask-wolfram (.substring message (count alpha-command)) wolfram-appid)]
            (doseq [line results]
                (send-message irc channel line))))
    (if (.startsWith message "!memory")
        (send-message irc channel
            (memory-stat)))
)

(defn -main
    [& args]
    (with-command-line
        args
        "Arguments: -channel #mychannel -nick botnick -server irc.server.com -appname wolfram-app-name -appid wolfram-app-id"
        [
            [channel c "IRC Channel to join." "#alphabottest"]
            [nick n "Bot's IRC nick." "alphabot"]
            [server s "IRC server address." "localhost"]
            [appid i "Application ID for Wolfram Alpha API." ""]
        ]
        (do
            (println channel nick server appid)
            (connect
                (create-irc {
                    :name nick
                    :server server
                    :fnmap {
                        :on-message #(on-message % appid)
                    }
                })
                :channels [channel])
)))


