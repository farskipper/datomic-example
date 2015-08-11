(ns db-fns
  (:require [datomic.api :as d]
            [clojure.string :refer [lower-case trim split blank?]]))

(defn str-blank? [s]
  (if (string? s)
    (blank? s)
    true))

(defn assert-non-blank-str [v]
  (assert (not (str-blank? v))))

(defn add-user! [db-conn username fname lname]
  (assert-non-blank-str username)
  (assert-non-blank-str fname)
  (assert-non-blank-str lname)

  (let [tmp-id (d/tempid :db.part/user)
        tx @(d/transact db-conn [{:db/id          tmp-id
                                  :user/username  username
                                  :user/firstname fname
                                  :user/lastname  lname}])]
    (d/resolve-tempid (d/db db-conn) (:tempids tx) tmp-id)))

(defn get-n-users [db]
  (or (d/q '[:find (count ?username) .
             :where [_ :user/username ?username]]
           db) 0))

(defn get-user [db id]
  (d/entity db id))
