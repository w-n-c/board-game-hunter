(ns board-game-hunter.db.core-test
  (:require
   [board-game-hunter.db.core :refer [*db*] :as db]
   [java-time :as jt]
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
    (migrations/migrate ["reset"] (select-keys env [:database-url]))
    (f)))

(defn create-test-hunter [t-conn]
  (db/create-hunter! t-conn
                     {:login "tester"
                      :name "I go by many names"
                      :password "notagoodone"}))

(defn get-test-hunter-auth [t-conn]
  (db/get-hunter-for-auth* t-conn {:login "tester"}))

(defn get-test-hunter-id [t-conn]
  (:id (get-test-hunter-auth t-conn)))

; we may hard-code the prey id because it comes from an external database
(defn create-test-prey [t-conn]
  (db/create-prey! t-conn
                   {:id 21241
                    :name "Neuroshima Hex! 3.0"
                    :bgg-url "https://boardgamegeek.com/boardgame/21241/neuroshima-hex-30"
                    :last-tracked (jt/local-date-time)}))

(defn get-test-prey [t-conn]
  (db/get-prey* t-conn {:id 21241}))

(defn create-test-hunt [t-conn]
  (create-test-hunter t-conn)
  (create-test-prey t-conn)
  (db/hunt-prey! t-conn
                 {:hunter-id (get-test-hunter-id t-conn)
                  :prey-id 21241}))

(deftest test-hunters
  (jdbc/with-transaction [t-conn *db* {:rollback-only true}]
    (is (= 1 (create-test-hunter t-conn)))
    (let [hunter (get-test-hunter-auth t-conn)]
          (is (= "tester" (:login hunter)))
          (is (= "I go by many names" (:name hunter)))
          (is (= "notagoodone" (:password hunter)))
          (is (instance? java.util.UUID (:id hunter))))))

(deftest test-prey
  (jdbc/with-transaction [t-conn *db* {:rollback-only true}]
    (is (= 1 (create-test-prey t-conn)))
    (let [prey (get-test-prey t-conn)]
      (is (= 21241 (:id prey)))
      (is (= "Neuroshima Hex! 3.0" (:name prey)))
      (is (= "https://boardgamegeek.com/boardgame/21241/neuroshima-hex-30" (:bgg_url prey)))
      (is (instance? java.time.LocalDateTime (:last_tracked prey))))))

(deftest test-hunts
  (jdbc/with-transaction [t-conn *db* {:rollback-only true}]
    (create-test-hunt t-conn)
    (let [id (get-test-hunter-id t-conn)
          hunt (first (db/get-hunts-by-hunter* t-conn {:id id}))]
      (is (= "I go by many names" (:hunter_name hunt)))
      (is (= 21241 (:prey_id hunt)))
      (is (= "Neuroshima Hex! 3.0" (:name hunt)))
      (is (= "https://boardgamegeek.com/boardgame/21241/neuroshima-hex-30" (:bgg_url hunt)))
      (is (instance? java.time.LocalDateTime (:last_tracked hunt))))))
