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
