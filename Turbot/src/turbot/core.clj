; :mode=clojure:

;; ## IRC bot showing recent chat history over http ##

(ns turbot.core
    (:gen-class)
    (:use
        [clojure.contrib.command-line]
        [irclj.core]
        [ring.adapter.jetty :only [run-jetty]]
        [ring.util.response :only [response redirect]]
        [ring.util.json-response :only [json-response]]
        [ring.middleware.content-type :only [wrap-content-type]]
        [ring.middleware.resource :only [wrap-resource]]
    )
)

(def empty-state {})

(defn fix-channel [c]
    (if (and c (not (= \# (first c))))
        (str \# c)
        c))

(defn on-any [allow-privmsg event state-ref]
    (let [{:keys [
        channel
        irc
        nick
        new-nick
        doing
        message
        target
        reason
        action?
        topic]} event]
        (if (and (nil? channel) (= doing "QUIT"))
            (doseq [[c {:keys [users]}] @state-ref]
                (if (some #{nick} users)
                    (on-any allow-privmsg (assoc event :channel c) state-ref)))
                (if (= (= doing "PRIVMSG") allow-privmsg)
                    (let [channel (fix-channel channel)]
                        (dosync
                            (if-let [channel-obj ((@irc :channels) channel)] (ref-set state-ref
                                (assoc @state-ref channel
                                    (assoc (@state-ref channel)
                                        :users (sort (map first (channel-obj :users)))
                                        :topic (channel-obj :topic)))))
                            (if channel
                                (ref-set state-ref (assoc-in @state-ref [channel :recentlines]
                                    (vec (take-last 1000
                                        (concat (:recentlines (@state-ref channel))
                                            [(into {} (filter val {
                                                :nick nick
                                                :newnick new-nick
                                                :doing doing
                                                :message message
                                                :target target
                                                :reason reason
                                                :isaction action?
                                                :topic topic
                                                :timestamp (System/currentTimeMillis)
                                            }))]))))))))))))

(defn web-handler [{uri :uri} state-ref]
    (condp = uri
        "/chat.json" (assoc-in (json-response @state-ref)
            [:headers "Cache-Control"] "max-age=2")
        "/" (redirect "/chat.html")))

(defn -main
    [& args]
    (with-command-line
        args
        "Arguments: [-nick botnick] [-server irc.server.com] #chan1 #chan2 .."
        [
            [nick n "Bot's IRC nick." "turbot"]
            [server s "IRC server address." "localhost"]
            channels
        ]
        (let [state-ref (ref empty-state)] (do
            (future (run-jetty (wrap-content-type (wrap-resource
                #(web-handler % state-ref) "public")) {:port 8000}))
            (connect
                (create-irc {
                    :name nick
                    :server server
                    :fnmap {
                        :on-message (fn [e]
                            (on-any true e state-ref))
                        :on-any (fn [e]
                            (on-any false e state-ref))
                    }
                })
                :channels channels)
))))



