(ns board-game-hunter.extern.bgg
  (:require
    [clojure.set :refer [rename-keys]]
    [clojure.string :as str]
    [clojure.xml :as xml]
    [jsonista.core :as j]
    [org.httpkit.client :as http])
  (:import
    [com.fasterxml.jackson.dataformat.xml XmlMapper]))

(def num-of-results 10)



(defn json->map [json]
  (j/read-value json j/keyword-keys-object-mapper))

(defn json->items [json]
  (:items (json->map json)))

(defn just-essentials [item]
  (select-keys item #{:yearpublished :rep_imageid :id :name :href}))

(defn bgg-keys->app-keys [item]
  (rename-keys item {:yearpublished :year-published :rep_imageid :image-id}))

(defn rename-path [item]
  (assoc item :href (str/replace (:href item) "boardgame" "prey")))

(defn json->results [json]
  (map (comp rename-path bgg-keys->app-keys just-essentials) (json->items body)))


; BGGs xmlapi does have a search method but it does not (to my knowledge) have pagination or result
; limiting. At time of writing, "final" returns 753 results in alphabetical order- not ideal for a
; responsive typahead. Instead, we are borrowing the typeahead off of their own home page search.
(defn typeahead [search]
  ; not sure what nosession 1 does but the bgg home page includes it when using the typeahead
  (let [opts
        {:query-params {"q" search "showcount" num-of-results "nosession" 1}
         :headers {"Accept" "application/json"}}
        {:keys [status body error] :as resp}
        @(http/get "https://boardgamegeek.com/search/boardgame?" opts)]

  (if (or error (not= 200 status))
    (throw (ex-info "Type ahead request failed" resp))
    (json->results body))))

(def xml-mapper (j/object-mapper {:mapper (XmlMapper.) :decode-key-fn true}))

(defn xml->results [xml]
  (j/read-value xml xml-mappoer))

(defn prey-details [prey-id]
  (let [opts
        {:query-params {"id" prey-id}}
        {:keys [status body error] :as resp}
        @(http/get "https://boardgamegeek.com/xmlapi2/thing")]

    (if (or error (not= 200 status))
      (throw (ex-info "Prey details request failed", resp))
      (xml->results body))))

;; details example 
;(clojure.pprint/write (j/read-value
;                        (:body @(http/get "https://boardgamegeek.com/xmlapi2/thing?id=277659"))
;                        (j/object-mapper {:mapper (XmlMapper.) :decode-key-fn true})))
