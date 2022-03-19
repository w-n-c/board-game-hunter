(ns board-game-hunter.extern.bgg
  (:require
    [clojure.set :refer [rename-keys]]
    [clojure.string :as str]
    [clojure.xml :as xml]
    [clojure.walk :as w]
    [clojure.java.io :as io]
    [jsonista.core :as j]
    [org.httpkit.client :as http])
  (:import
    [com.fasterxml.jackson.dataformat.xml XmlMapper]))

(def typeahead-result-count 10)

(def mapper-factory
  (memoize
    (fn [content-type]
      (let [mapper (case content-type
                         :xml (j/object-mapper {:mapper (XmlMapper.) :decode-key-fn true})
                         :json j/keyword-keys-object-mapper)]
        (fn [input] (j/read-value input mapper))))))

(defn bgg-results-filter [item]
  (select-keys item #{:yearpublished :rep_imageid :id :name :href}))

(defn bgg-keys->bgh-keys [item]
  (rename-keys item {:yearpublished :year-published
                     :rep_imageid   :image-id
                     :playingtime   :play-time
                     :minplaytime   :min-play-time
                     :minplayers    :min-players
                     :maxplayers    :max-players}))

(defn rename-path [item]
  (assoc item :href (str/replace (:href item) "boardgame" "prey")))

(defn typeahead-json->bgg-results [json]
  (:items ((mapper-factory :json) json)))

(defn bgg-results->prey-results [bgg-results]
  (map (comp rename-path
             bgg-keys->bgh-keys
             bgg-results-filter) bgg-results))

(defn typeahead-json->prey-results [json]
  (-> json
      (typeahead-json->bgg-results)
      (bgg-results->prey-results)))

; The mapper does not handle array-as-multiple-of-same-tag xml well, which BGG does use extensively.
; It is (probably) configurably fixable but, fortunately, we do not need any of that array data.
(defn bgg-details-xml->bgg-details [xml]
  (:item ((mapper-factory :xml) xml)))

(defn prey-details-filter [prey]
  (select-keys prey #{:image :description :name :maxplayers :minplayers :playingtime :minplaytime :yearpublished :thumbnail}))

(defn bgg-details->prey-details [prey]
  ((comp bgg-keys->bgh-keys prey-details-filter) prey))

(defn is-value-map [input]
  (and (map? input)
       (contains? input :value)))

; because we do not check if the map only contains :value, this will wipe attributes associated
; with the xml tag. This is a deliberate choice to have full consistency easily made because
; we do not use any keys with valuable attributes
(defn value-flatten [input]
  (if (is-value-map input)
    (:value input)
    input))
  ;(w/postwalk #(if (is-value-map %) (:value %) %) input))

(defn string->number [input]
  (if (string? input)
    (let [n (read-string input)]
      (if (number? n) n input))
    input))

(defn reformat [input] (w/postwalk (comp value-flatten string->number) input))


(defn bgg-details-xml->prey-details [xml]
  (-> xml
      (bgg-details-xml->bgg-details)
      (bgg-details->prey-details)
      (reformat)))

; BGG's xmlapi does have a search method but it does not (to my knowledge) have pagination or
; result limiting. At time of writing, "final" returns 753 results in alphabetical order- not
; ideal for a responsive typahead. Instead, we are borrowing the (json) typeahead off of their
; home page.
(defn typeahead [search]
  ; not sure what nosession 1 does but the bgg home page includes it when using the typeahead
  (let [opts
        {:query-params {"q" search "showcount" typeahead-result-count "nosession" 1}
         :headers {"Accept" "application/json"}}
        {:keys [status body error] :as resp}
        @(http/get "https://boardgamegeek.com/search/boardgame?" opts)]

    (if (or error (not= 200 status))
      (throw (ex-info "Type ahead request failed" resp))
      (typeahead-json->prey-results body))))

; Loads details about a given game from xml api (player count, playtime, image urls etc)
(defn prey-details [prey-id]
  (let [opts
        {:query-params {"id" prey-id}}
        {:keys [status body error] :as resp}
        @(http/get "https://boardgamegeek.com/xmlapi2/thing" opts)]

    (if (or error (not= 200 status))
      (throw (ex-info "Prey details request failed", resp))
      (bgg-details-xml->prey-details body))))
