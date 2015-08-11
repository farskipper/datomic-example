# datomic-example
An example of how I setup and use datomic.

This example is just some database functions (`src/db_fns.clj`) and their tests (`test/db_fns_test.clj`). If you look at `resources/datomic/schema.edn` you'll see that the data model is for a simple messaging app. Users can send messages to each other, and there can be more than one recipient per message.

# how to run it
Be sure leiningen [leiningen](http://leiningen.org/) is installed.

Then run the tests
```sh
$ lein auto test
```
It'll automatically re-run when you change code
