(ns board-game-hunter.core
  (:require
    [kee-frame.core :as kf]
    [re-frame.core :as rf]
    [reagent.dom :as rdom]
    [board-game-hunter.ajax :as ajax]
    [board-game-hunter.routing :as routing]
    [board-game-hunter.routes.app :as app]
    [board-game-hunter.view :as view]
    ))

;; -------------------------
;; Initialize app
(defn ^:dev/after-load mount-components
  []
  (rf/clear-subscription-cache!)
  (kf/start! {:routes         (app/app-routes)
              :log            {:level        :debug
                               :ns-blacklist ["kee-frame.event-logger"]}
              :root-component [view/root-component]}))

(defn init! []
  (ajax/load-interceptors!)
  (mount-components))
