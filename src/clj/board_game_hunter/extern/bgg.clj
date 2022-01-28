(ns board-game-hunter.extern.bgg
  (:require [org.httpkit.client :as http]
            [jsonista.core :as j]
            [clojure.set :refer [rename-keys]]))


(def num-of-results 10)

(defn json->map [json]
  (j/read-value json j/keyword-keys-object-mapper))

(defn json->items [json]
  (:items (json->map json)))

(defn just-essentials [item]
  (select-keys item #{:yearpublished :rep_imageid :id :name :href}))

(defn bgg-keys->app-keys [item]
  (rename-keys item {:yearpublished :year-published :rep_imageid :image-id}))

(defn body->results [body]
  (map (comp bgg-keys->app-keys just-essentials) (json->items body)))

(defn type-ahead [search]
  (let [opts {:query-params {"q" search "showcount" num-of-results "nosession" 1}
              :headers {"Accept" "application/json"}}
        {:keys [status body error] :as resp} @(http/get "https://boardgamegeek.com/search/boardgame?" opts)]
  (if (or error (not= 200 status))
    (throw (ex-info "Type ahead request failed" resp))
    (body->results body))))
