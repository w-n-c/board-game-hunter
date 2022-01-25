(ns board-game-hunter.views.home-test
  (:require [cljs.test :refer-macros [is deftest use-fixtures async]]
            [pjstadig.humane-test-output]
            [day8.re-frame.test :as rf-test]
            [re-frame.core :as rf]
            [re-frame.db :refer [app-db]]
            [reagent.dom :as rdom]
            [board-game-hunter.views.home :as home]))

(defn reset-re-frame [] (reset! app-db {}))

(use-fixtures :each {:before reset-re-frame})

(deftest test-home-page
    (let [search         (rf/subscribe [::home/search])
          home-page      (home/home-page)]

      ;Input is synced to search
      (rf/dispatch-sync [::home/set-search "Neuroshima"])
      (is (= @search @(get-in home-page [1 1 :subscription])))

      ;on-change is synced to set-search and only dispatches change after 300 ms
      (async done
          ((get-in home-page [1 1 :dispatch :on-change]) "debounced")
          (js/setTimeout #(is (= "Neuroshima" @search))
                         300)
          (js/setTimeout #(do
                            (is (= "debounced" @search))
                            (done))
                         305))))


(deftest test-home-re-frame
  (rf-test/run-test-sync
    (let [search         (rf/subscribe [::home/search])
          error          (rf/subscribe [::home/error])
          search-results (rf/subscribe [::home/search-results])]
      ; Assert initial state
      (is (nil? @search))
      (is (nil? @error))
      (is (nil? @search-results))

      ; Dispatch event
      (rf/dispatch [::home/set-search "Neuroshima"])
      (rf/dispatch [::home/set-error "403 Unauthorized"])
      (rf/dispatch [::home/set-search-results "temp"])

      ; Assert new state
      (is (= "Neuroshima" @search))
      (is (= "403 Unauthorized" @error))
      (is (= "temp" @search-results)))))
