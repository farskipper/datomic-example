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
