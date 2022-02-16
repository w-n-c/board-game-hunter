;; Copied from https://github.com/MattiNieminen/re-fill
;; because its unmaintained, undeployed, untested and has has more than just 'debounce'

;; Copyright © 2017-2018 Metosin Copyright © 2017-2019 Matti Nieminen
;; Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.

(ns board-game-hunter.utils.debounce
  (:require [re-frame.core :as rf]
            [re-frame.db :as db]))

(rf/reg-fx
 :bgh/debounce
 (fn [[event timeout]]
   (let [id (first event)]
     (js/clearTimeout (get-in @db/app-db [:bgh/debounce id]))
     (swap! db/app-db
            assoc-in
            [:bgh/debounce id]
            (js/setTimeout (fn []
                             (rf/dispatch event)
                             (swap! db/app-db
                                    assoc
                                    :bgh/debounce
                                    (dissoc (:bgh/debounce @db/app-db) id)))
                           (or timeout 100))))))

(rf/reg-fx
 :bgh/stop-debounce
 (fn [id]
   (js/clearTimeout (get-in @db/app-db [:bgh/debounce id]))
   (swap! db/app-db
          assoc
          :bgh/debounce
          (dissoc (:bgh/debounce @db/app-db) id))))

(rf/reg-event-fx
 :bgh/debounce
 (fn [_ [_ options timeout]]
   {:bgh/debounce [options timeout]}))

(rf/reg-event-fx
 :bgh/stop-debounce
 (fn [_ [_ id]]
   {:bgh/stop-debounce id}))

(rf/reg-sub
 :bgh/debounce
 (fn [db _]
   (:bgh/debounce db)))
