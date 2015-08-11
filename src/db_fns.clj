(ns db-fns
  (:require [datomic.api :as d]
            [clojure.string :refer [lower-case trim split blank?]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; fns for asserting valid inputs

(defn str-blank? [s]
  (if (string? s)
    (blank? s)
    true))

(defn assert-non-blank-str [v]
  (assert (not (str-blank? v))))

(defn assert-valid-user-id [db id]
  (assert (= 1 (d/q '[:find  (count ?username) .
                      :in    $ ?id
                      :where [?id :user/username ?username]]
                    db
                    id))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; user db fns

(defn add-user! [conn username fname lname]
  (assert-non-blank-str username)
  (assert-non-blank-str fname)
  (assert-non-blank-str lname)

  (let [tmp-id (d/tempid :db.part/user)
        tx @(d/transact conn [{:db/id          tmp-id
                               :user/username  username
                               :user/firstname fname
                               :user/lastname  lname}])]
    (d/resolve-tempid (d/db conn) (:tempids tx) tmp-id)))

(defn get-n-users [db]
  (or (d/q '[:find (count ?username) .
             :where [_ :user/username ?username]]
           db) 0))

(defn get-user [db id]
  (d/entity db id))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; message db fns

(defn send-message! [conn from-id text & to-ids]
  (let [db (d/db conn)]
    (assert-valid-user-id db from-id)
    (assert-non-blank-str text)
    (doseq [id to-ids]
      (assert-valid-user-id db id)))

  (let [tmp-id (d/tempid :db.part/user)
        tx @(d/transact conn [{:db/id                tmp-id
                               :message/from-user-id from-id
                               :message/text         text
                               :message/to-user-id   to-ids}])]
    (d/resolve-tempid (d/db conn) (:tempids tx) tmp-id)))

(defn get-n-messages [db]
  (or (d/q '[:find (count ?id) .
             :where [?id :message/from-user-id]]
           db) 0))
