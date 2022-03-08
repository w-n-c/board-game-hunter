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

(def mapper-factory
  (memoize
    (fn [content-type]
      (let [mapper (case content-type
                         :xml (j/object-mapper {:mapper (XmlMapper.) :decode-key-fn true})
                         :json j/keyword-keys-object-mapper)]
        (fn [input] (j/read-value input mapper))))))

(defn xml->response [xml]
  ((mapper-factory :xml) xml))


(defn just-essentials [item]
  (select-keys item #{:yearpublished :rep_imageid :id :name :href}))

(defn bgg-keys->app-keys [item]
  (rename-keys item {:yearpublished :year-published :rep_imageid :image-id}))

(defn rename-path [item]
  (assoc item :href (str/replace (:href item) "boardgame" "prey")))

(defn typeahead-json->bgg-results [json]
  (:items ((mapper-factory :json) json)))

(defn bgg-results->bgh-results [bgg-results]
  (map (comp rename-path bgg-keys->app-keys just-essentials)))

(defn typeahead-json->bgh-results [json]
  (-> json
      (typeahead-json->bgg-results)
      (bgg-results->bgh-results)))

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
     (typeahead-json->bgg-results body))))


(defn prey-details [prey-id]
  (let [opts
        {:query-params {"id" prey-id}}
        {:keys [status body error] :as resp}
        @(http/get "https://boardgamegeek.com/xmlapi2/thing" opts)]

    (if (or error (not= 200 status))
      (throw (ex-info "Prey details request failed", resp))
      (xml->response body))))


;; details example 
;(clojure.pprint/write (j/read-value
;                        (:body @(http/get "https://boardgamegeek.com/xmlapi2/thing?id=277659"))
;                        (j/object-mapper {:mapper (XmlMapper.) :decode-key-fn true})))
