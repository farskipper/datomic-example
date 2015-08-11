(defproject mrwrite.datomic-example "1.0.0"
  :description  "An example of how I setup and use datomic"

  :dependencies [[org.clojure/clojure  "1.7.0"]
                 [com.datomic/datomic-free  "0.9.5206" :exclusions  [joda-time]]]

  :plugins      [[lein-auto  "0.1.2"]])
