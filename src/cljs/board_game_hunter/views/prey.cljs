(ns board-game-hunter.views.prey
  (:require [re-frame.core :as rf]
            [kee-frame.core :as kf]
            [ajax.core :as http]
            [board-game-hunter.components :refer [text-input]]))

;(kf/reg-controller
;  ::prey-controller
;  {:params (fn [{:keys [handler route-params]}]
;             (when (= handler :prey)
;               route-params))})

;(rf/reg-event-fx
;  ::load-prey
;  (fn [{:keys [db]} [_ url]]    ;; <--- new: obtain db and item-id directly
;    {:db (assoc db ::url url)}))
;
;(rf/reg-sub
;  ::load-prey
;  (fn [db _]
;    (::url db)))

(defonce embed-bgg [])

(defn prey-page [{{:keys [id name]} :path-params}]
  [:section.section>div.container>div.content
   ;[:div (str "::load-prey " @(rf/subscribe [::load-prey]))]
   ;[:div {:id "bgg-page"}]
   [:div {:dangerouslySetInnerHTML {:__html (str "<iframe src='https://boardgamegeek.com/boardgame/" id "/" name "' />")}}]
   [:div (str "input: " id)]])
