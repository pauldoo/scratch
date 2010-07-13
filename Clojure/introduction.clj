#!/usr/bin/env clj

(def visitors (ref #{}))

(defn hello
  "Writes hello message to *out*.  Calls you by username.
  Knows if you have been here before."
  [username]
  (dosync
    (let [past-visitor (@visitors username)]
      (if past-visitor
        (str "Welcome back, " username)
        (do
          (alter visitors conj username)
          (str "Hello, " username))))))

(println (hello "Bob"))
(println (hello "Cat"))
(println (hello "Bob"))
(println @visitors)

