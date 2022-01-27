(ns board-game-hunter.extern.bgg-test
  (:require [clojure.test :refer [use-fixtures deftest testing is]]
            [mount.core :as mount]
            [board-game-hunter.extern.bgg :as bgg]
            [clojure.java.io :as io]))

(use-fixtures
  :once
  (fn [f]
    (mount/start #'board-game-hunter.config/env)
    (f)))

(defn promisify [response]
  (deliver (promise) response))

(deftest bgg-requests
  (with-redefs [org.httpkit.client/request
                (fn [_ _] (promisify {:status 200 :body (slurp (io/resource "bgg-samples/type-ahead.json"))}))]
    (testing "type-ahead"
      (let [response (bgg/type-ahead ["final"])
            game (second (:body response))]
        (is (= 200 (:status response)))
        (is (= "Final Girl" (:name game)))
        (is (= 2021 (:yearpublished game)))
        (is (= 6520382 (:rep_imageid game)))
        (is (= "/boardgame/277659/final-girl" (:href game)))))))
