(ns board-game-hunter.extern.bgg
  (:require [org.httpkit.client :as http]
            [jsonista.core :as j]))


(def num-of-results 10)
(defn type-ahead [search]
  (let [opts {:query-params {"q" search "showcount" num-of-results "nosession" 1}
              :headers {"Accept" "application/json"}}
        {:keys [status headers body error] :as resp} @(http/get "https://boardgamegeek.com/search/boardgame?" opts)]
  (if error
    (println "Failed, exception: " error)
    {:status status :body (:items (j/read-value body j/keyword-keys-object-mapper))})))
