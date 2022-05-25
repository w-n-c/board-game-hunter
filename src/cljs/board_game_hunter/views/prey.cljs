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

(defn listing-table [listings]
  [:table.table
   [:thead>tr
    [:th "link"]
    [:th "condition"]
    [:th "price"]
    [:th "notes"]]
   [:tbody
    (for [{:keys [link condition notes price]} listings]
      (let [href (:href link)
            cost (:value price)
            currency (:currency price)]
        [:tr 
          [:td>a {:href href} href]
          [:td (cond 
                 (= condition "likenew") "like new"
                 (= condition "verygood") "very good"
                 :else condition)]
          [:td cost " " currency]
          [:td notes]]))]])

(defn prey-page [{{:keys [id name]} :path-params}]
  [:section.section>div.container>div.content
   (let [prey-details @(rf/subscribe [::prey-details])
         display-name (:value (first (filter #(= "primary" (:type %)) (:name prey-details))))]
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
       [:p (:description prey-details)]]
      (listing-table (:listing (:listings prey-details)))])])
