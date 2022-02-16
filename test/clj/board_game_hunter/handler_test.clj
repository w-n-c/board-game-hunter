(ns board-game-hunter.handler-test
  (:require
    [clojure.test :refer :all]
    [ring.mock.request :refer :all]
    [board-game-hunter.handler :refer :all]
    [board-game-hunter.extern.bgg :as bgg]
    [board-game-hunter.middleware.formats :as formats]
    [muuntaja.core :as m]
    [mount.core :as mount]))

(defn parse-json [body]
  (m/decode formats/instance "application/json" body))

(use-fixtures
  :once
  (fn [f]
    (mount/start #'board-game-hunter.config/env
                 #'board-game-hunter.handler/routes)
    (f)))

(deftest test-app
  (testing "main route"
    (let [response ((app) (-> (request :post "/api/math/plus")
                              (json-body {:x 10 :y 20})))]
      (is (= 200 (:status response)))
      (is (= 30 (:total (parse-json (:body response)))))))
  (testing "search service"
    (with-redefs [bgg/type-ahead
                  (fn [search] (lazy-seq [{:id "277659"
                                           :name "Final Girl"}]))]
    (let [response ((app) (request :get "/api/bgg/search?s=final"))
          body (parse-json (:body response))]
      (is (= 200 (:status response)))
      (is (= 1 (count body)))
      (is (= "277659" (:id (first body))))
      (is (= "Final Girl" (:name (first body)))))))

  (testing "not-found route"
    (let [response ((app) (request :get "/invalid"))]
      (is (= 404 (:status response))))))
