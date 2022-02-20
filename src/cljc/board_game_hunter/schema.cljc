(ns board-game-hunter.schema
  (:require [clojure.spec.alpha :as s]))

; we should define another schema file with the bgg spec then coerce to our spec?
(s/def ::bgg-id string?)
