(ns board-game-hunter.extern.bgg
  (:require
   [clojure.set :refer [rename-keys]]
   [clojure.string :as str]
   [clojure.walk :as w]
   [jsonista.core :as j]
   [org.httpkit.client :as http])
  (:import
   [org.apache.commons.text StringEscapeUtils]
   [com.fasterxml.jackson.dataformat.xml XmlMapper]
   [com.fasterxml.jackson.databind JsonNode ObjectMapper]))

(def typeahead-result-count 10)

(extend-protocol j/ReadValue
  JsonNode
  (-read-value [this ^ObjectMapper mapper]
    (.treeToValue mapper this ^Class Object)))

(defn read-json [input]
  (j/read-value input j/keyword-keys-object-mapper))

(defn read-xml [input]
  (let [xml-mapper (XmlMapper.)
        j-mapper (j/object-mapper {:mapper xml-mapper :decode-key-fn true})]
    (j/read-value (.readTree xml-mapper input) j-mapper)))

(defn bgg-results-filter [item]
  (select-keys item #{:yearpublished :rep_imageid :id :name :href}))

(defn bgg-keys->bgh-keys [item]
  (w/postwalk-replace {:yearpublished       :year-published
                       :rep_imageid         :image-id
                       :playingtime         :play-time
                       :minplaytime         :min-play-time
                       :minplayers          :min-players
                       :maxplayers          :max-players
                       :marketplacelistings :listings
                       :marketinfo          :market-info
                       :lastcomment         :last-comment
                       :lastbid             :last-bid
                       :userid              :user-id
                       :marketrating        :market-rating
                       :traderating         :trade-rating
                       :postdate            :post-date}
                      item))

(defn rename-path [item]
  (assoc item :href (str/replace (:href item) "boardgame" "prey")))

(defn typeahead-json->bgg-results [json]
  (:items (read-json json)))

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
  (:item (read-xml xml)))

(defn prey-details-filter [prey]
  (select-keys prey #{:marketplacelistings :image :description :name :maxplayers :minplayers :playingtime :minplaytime :yearpublished :thumbnail}))

(defn bgg-details->prey-details [prey]
  ((comp bgg-keys->bgh-keys prey-details-filter) prey))

(defn is-value-map [input]
  (and (map? input)
       (= 1 (count input))
       (contains? input :value)))

(defn flatten-value [input]
  (if (is-value-map input)
    (:value input)
    input))

(defn flatten-value-only [input] (w/postwalk flatten-value input))

(def keys-to-unescape #{:description :notes})

(defn unescape [input]
  (StringEscapeUtils/unescapeHtml4 input))

(defn unescape-walked-input [input]
  (if (and (vector? input)
           (contains? keys-to-unescape (first input)))
    [(first input) (unescape (second input))]
    input))

(defn unescape-content [input]
  (w/postwalk unescape-walked-input input))

(defn bgg-details-xml->prey-details [xml]
  (-> xml
      (bgg-details-xml->bgg-details)
      (bgg-details->prey-details)
      (flatten-value-only)
      (unescape-content)))

(defn map-print [input]
  (clojure.pprint/pprint input)
  input)

(defn bgg-auction-json->bgg-auction-details [json]
  (:items (first (map-print (read-json json)))))

(defn auction-details-filter [auction]
  (dissoc auction :bodyXml :descriptionXml))


(defn bgg-auction-details->auction-details [auctions]
  (map (comp bgg-keys->bgh-keys auction-details-filter) auctions))


(defn bgg-auction-json->auction-details [json]
  (-> json
      (bgg-auction-json->bgg-auction-details)
      (bgg-auction-details->auction-details)))

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
        {:query-params {"id" prey-id "marketplace" 1}}
        {:keys [status body error] :as resp}
        @(http/get "https://boardgamegeek.com/xmlapi2/thing" opts)]

    (if (or error (not= 200 status))
      (throw (ex-info "Prey details request failed" resp))
      (bgg-details-xml->prey-details body))))

(defn auction-search [prey-id]
  (let [opts
        {:query-params {"id" prey-id "type" "things"}}
        {:keys [status body error] :as resp}
        @(http/get "https://api.geekdo.com/api/geekshopper" opts)]
    (if (or error (not= 200 status))
      (throw (ex-info "Auction request failed" resp))
      (bgg-auction-json->auction-details body))))
