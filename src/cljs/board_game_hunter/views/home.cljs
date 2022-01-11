(ns board-game-hunter.views.home
  (:require [re-frame.core :as rf]
            [kee-frame.core :as kf]
            [ajax.core :as http]
            [markdown.core :refer [md->html]]))


(kf/reg-controller
  ::home-controller
  {:params (constantly true)
   :start  [::load-home-page]})

(rf/reg-sub
  :docs
  (fn [db _]
    (:docs db)))

(kf/reg-chain
  ::load-home-page
  (fn [_ _]
    {:http-xhrio {:method          :get
                  :uri             "/docs"
                  :response-format (http/raw-response-format)
                  :on-failure      [:common/set-error]}})
  (fn [{:keys [db]} [_ docs]]
    {:db (assoc db :docs docs)}))

(defn home-page []
  [:section.section>div.container>div.content
   (when-let [docs @(rf/subscribe [:docs])]
     [:div {:dangerouslySetInnerHTML {:__html (md->html docs)}}])])
