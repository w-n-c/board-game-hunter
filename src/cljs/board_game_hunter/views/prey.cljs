(ns board-game-hunter.views.prey
  (:require [re-frame.core :as rf]
            [kee-frame.core :as kf]
            [ajax.core :as http]
            [board-game-hunter.components :refer [text-input]]))

(kf/reg-chain ::load-prey-details
              (fn [_ [id]]
                {:http-xhrio {:method :get
                              :uri    (str "/api/prey/" id)
                              :request-format (http/transit-request-format)
                              :response-format (http/transit-response-format) }})
              (fn [{:keys [db]} [_ prey-details]]
                {:db (assoc db ::prey-details prey-details)}))

(kf/reg-controller
  :prey
  {:params (fn [{:keys [data path-params] :as route-data}]
             (when (= (:name data) :prey)
               (:id path-params)))

   :start [::load-prey-details]})

(rf/reg-sub
  ::prey-details
  (fn [db _]
    (::prey-details db)))

(defn prey-page [{{:keys [id name]} :path-params}]
  [:section.section>div.container>div.content
   (let [prey-details @(rf/subscribe [::prey-details])
         api-name     (:name prey-details)
         display-name (if (list? api-name) (first api-name) api-name)]
     [:div.container
      [:h2 display-name " (" (:year-published prey-details) ") "]
      [:img {:src (:thumbnail prey-details)}]
      [:section.container
       [:h3 "Game Info"]
       [:p "Player Count: " (:min-players prey-details) "-" (:max-players prey-details)]
       [:p "Playtime: " (:min-play-time prey-details) "-" (:play-time prey-details) " minutes"]]
      [:section.container
       [:br]
       [:h3 "Description"]
       [:p (:description prey-details)]]])])
