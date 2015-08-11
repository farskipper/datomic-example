(ns db-fns-test
  (:require [clojure.test :refer :all]
            [datomic.api :as d]
            [db-fns :refer :all]))

(defn make-db []
  (let [url "datomic:mem://test"]
    (d/delete-database url)
    (d/create-database url)
    (let [conn (d/connect url)
          schema (load-file "resources/datomic/schema.edn")]
      (d/transact conn schema)
      conn)))

(defn easy-setup []
  (let [conn  (make-db)
        ;some fns to make setup easier
        add!  (fn [uname fname lname]
                (add-user! conn uname fname lname))
        send! (fn [from text & to]
                (let [db (d/db conn)]
                  (apply send-message!
                         conn
                         (find-user-id db from)
                         text
                         (map (partial find-user-id db) to))))]
    [conn add! send!]))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(deftest test-add-user!
  (let [conn (make-db)]

    (is (= 0 (get-n-users (d/db conn))))

    (add-user! conn "jdoe" "John" "Doe")

    (try
      (add-user! conn "" "John" "Doe")
      (is false)
      (catch AssertionError e
        (is true)))

    (is (= 1 (get-n-users (d/db conn))))))


(deftest test-send-message!
  (let [conn  (make-db)
        andy  (add-user! conn "andy"  "Andy"   "Taylor")
        gomer (add-user! conn "gomer" "Gomer"  "Pyle")
        barn  (add-user! conn "barn"  "Barney" "Fife")]
    (is (= 3 (get-n-users (d/db conn))))

    (is (= 0 (get-n-messages (d/db conn))))

    (send-message! conn andy "You beat ever' thing, you know that?" barn)
    (is (= 1 (get-n-messages (d/db conn))))

    (send-message! conn gomer "howdy, y'all" andy barn)
    (is (= 2 (get-n-messages (d/db conn))))))


(deftest test-find-user-id
  (let [conn (make-db)
        db0  (d/db conn)
        andy (add-user! conn "andy"  "Andy"   "Taylor")
        db1  (d/db conn)
        barn (add-user! conn "barn"  "Barney" "Fife")
        db2  (d/db conn)]

    (is (= nil  (find-user-id db0 "andy")))
    (is (= andy (find-user-id db1 "andy")))
    (is (= andy (find-user-id db1 "  AnDy  ")))
    (is (= nil  (find-user-id db1 "barn")))
    (is (= barn (find-user-id db2 "barn")))))


(deftest test-my-inbox
  (let [[conn add! send!] (easy-setup)]

    (add! "andy" "Andy"   "Taylor")
    (add! "barn" "Barney" "Fife")
    (add! "floyd" "Floyd"  "Lawson")

    (send! "andy" "Hey, you don't supose..."
           "barn")
    (send! "barn" "...well, you're not talking to a jerk you know!"
           "andy")

    (send! "barn" "We defy the Mafia!!!"
           "andy" "floyd")

    (send! "floyd" "I'm talking to myself"
           "floyd")

    (let [db (d/db conn)]

      (is (= (my-inbox db "blah")
             #{}))

      (is (= (my-inbox db "andy")
             #{["barn" "We defy the Mafia!!!"]
               ["barn" "...well, you're not talking to a jerk you know!"]}))

      (is (= (my-inbox db "floyd")
             #{["barn" "We defy the Mafia!!!"]
               ["floyd"  "I'm talking to myself"]})))))


(deftest test-everyone-ive-messaged-with
  (let [[conn add! send!] (easy-setup)]

    (add! "andy" "Andy"   "Taylor")
    (add! "barn" "Barney" "Fife")
    (add! "abee"  "Aunt"   "Bee")
    (add! "lou"   "Thelma" "Lou")
    (add! "gomer" "Gomer"  "Pyle")
    (add! "floyd" "Floyd"  "Lawson")

    (send! "andy" "Hey, you don't supose..."
           "barn")

    (send! "barn" "...well, you're not talking to a jerk you know!"
           "andy")

    (send! "gomer" "I reckon' the cellar would be downstairs"
           "barn")

    (send! "abee" "Now don't forget"
           "andy")

    (send! "barn" "We defy the Mafia!!!"
           "andy" "abee" "lou" "gomer" "floyd")

    (send! "floyd" "I'm talking to myself"
           "floyd")

    (let [db (d/db conn)]
      (is (= (everyone-ive-messaged-with db "andy")
             #{"barn" "abee"}))

      (is (= (everyone-ive-messaged-with db "lou")
             #{"barn"}))

      (is (= (everyone-ive-messaged-with db "barn")
             #{"floyd"  "abee"  "lou"  "andy"  "gomer"})))))
