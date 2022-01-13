(ns board-game-hunter.db.core-test
  (:require
   [board-game-hunter.db.core :refer [*db*] :as db]
   [java-time.pre-java8]
   [luminus-migrations.core :as migrations]
   [clojure.test :refer :all]
   [next.jdbc :as jdbc]
   [board-game-hunter.config :refer [env]]
   [mount.core :as mount]))

(use-fixtures
  :once
  (fn [f]
    (mount/start
     #'board-game-hunter.config/env
     #'board-game-hunter.db.core/*db*)
    (migrations/migrate ["migrate"] (select-keys env [:database-url]))
    (f)))

(deftest test-users
  (jdbc/with-transaction [t-conn *db* {:rollback-only true}]
    (is (= 1 (db/create-hunter!
              t-conn
              {:login "tester"
               :name "I go by many names"
               :password "notagoodone"})))
    (let [hunter (db/get-hunter-for-auth* t-conn {:login "tester"})]
          (is (= "tester" (:login hunter)))
          (is (= "I go by many names" (:name hunter)))
          (is (= "notagoodone" (:password hunter)))
          (is (instance? java.util.UUID (:id hunter))))))
