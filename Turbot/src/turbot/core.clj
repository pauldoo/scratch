; :mode=clojure:

;; ## IRC bot showing recent chat history over http ##

(ns turbot.core
    (:gen-class)
    (:use
        [clojure.contrib.command-line]
        [irclj.core]
        [ring.adapter.jetty :only [run-jetty]]
        [ring.util.response :only [response redirect]]
        [ring.middleware.resource :only [wrap-resource]]
    )
)

(def empty-state {
    :recent-lines []
})

(defn on-message [{:keys [nick channel message irc]} state-ref]
    (dosync (ref-set state-ref (assoc @state-ref
        :recent-lines (take-last 1000
                (concat (:recent-lines @state-ref) [(str nick ": " message)]))))))

(defn web-handler [{uri :uri} state-ref]
    (condp = uri
        "/chat" (response (apply str (map #(str % "\n") (:recent-lines @state-ref))))
        (redirect "/index.html")))

(defn -main
    [& args]
    (with-command-line
        args
        "Arguments: -channel #mychannel -nick botnick -server irc.server.com"
        [
            [channel c "IRC Channel to join." "#sprafftest"]
            [nick n "Bot's IRC nick." "turbot"]
            [server s "IRC server address." "localhost"]
        ]
        (let [state-ref (ref empty-state)] (do
            (println channel nick server)
            (future (run-jetty (wrap-resource
                #(web-handler % state-ref) "public") {:port 3000}))
            (connect
                (create-irc {
                    :name nick
                    :server server
                    :fnmap {
                        :on-message #(on-message % state-ref)
                    }
                })
                :channels [channel])
))))



