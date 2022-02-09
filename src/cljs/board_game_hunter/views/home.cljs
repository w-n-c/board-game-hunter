(ns board-game-hunter.views.home
  (:require [re-frame.core :as rf]
            [ajax.core :as http]
            [board-game-hunter.components :refer [text-input submit-btn]]
            [board-game-hunter.utils.debounce]
            [clojure.string :refer [blank?]]
            ;["strict-uri-encode" :as encode]
            ))

(rf/reg-sub
  ::search
  (fn [db _]
    (::search db)))

(rf/reg-sub
  ::searching?
  :<- [::search]
  (fn [[search] _]
    (not (blank? search))))

(rf/reg-sub
  ::error
  (fn [db _]
    (::error db)))

(rf/reg-event-db
  ::set-error
  (fn [db [_ error]]
    (assoc db ::error error)))

(rf/reg-sub
  ::search-results
  (fn [db _]
    (::search-results db)))

(defn set-search-results [db [_ response]]
  (assoc db ::search-results response))

(rf/reg-event-db
  ::set-search-results
  set-search-results)

(defn set-search [{:keys [db]} [_ search]]
  (merge
    {:db (assoc db ::search search)}
    (when-not (blank? search)
      {:http-xhrio {:method          :get
                    :uri             (str "/api/bgg/search?s=" search)
                    :request-format  (http/transit-request-format)
                    :response-format (http/transit-response-format)
                    :on-failure      [::set-error]
                    :on-success      [::set-search-results]}})))
(rf/reg-event-fx
  ::set-search
  set-search)


(defn home-page []
  [:section.section>div.container>div.content
   [text-input {:attrs {:name "search"
                        :prompt "Enter the name of the game you would like to track"}
                :subscription (rf/subscribe [::search])
                :dispatch {:on-change #(rf/dispatch [:bgh/debounce [::set-search %] 300])}}]
   [:div.container.dropdown {:class (if @(rf/subscribe [::searching?]) "is-active")}
    [:div.dropdown-menu {:role "menu"}
     [:div.dropdown-content
      (for [{:keys [id href name year-published]} @(rf/subscribe [::search-results])]
        [:a.dropdown-item {:key id :href href} (str name " " year-published)])]]]])
