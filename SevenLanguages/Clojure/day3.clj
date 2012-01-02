(def accounts (ref {}))

(defn create-account [n]
    (dosync
        (ref-set accounts (assoc @accounts n 0))))

(defn deposit [n a]
    (dosync
        (let [new-amount (+ (@accounts n) a)]
            (ref-set accounts (assoc @accounts n new-amount))
            new-amount)))



(def haircuts-done (atom 0))
(def customers-waiting (ref 0))
(def chairs-free (ref 3))

(defn barber-go []
    (if (dosync
        (if
            (> @customers-waiting 0)
            (do
                (ref-set chairs-free (inc @chairs-free))
                (ref-set customers-waiting (dec @customers-waiting))
                true)
            false))
        (do
            (Thread/sleep 20)
            (future (barber-go))
            (swap! haircuts-done inc))
        (recur)))

(defn customer-loop []
    (Thread/sleep (+ (rand 20) 10))
    (future (customer-loop))
    (dosync
        (if
            (> @chairs-free 0)
            (do
                (ref-set chairs-free (dec @chairs-free))
                (ref-set customers-waiting (inc @customers-waiting))))))


(future (customer-loop))
(future (barber-go))

(Thread/sleep 10000)
(println @haircuts-done)



