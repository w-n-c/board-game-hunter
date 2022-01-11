(ns board-game-hunter.env
  (:require
    [selmer.parser :as parser]
    [clojure.tools.logging :as log]
    [board-game-hunter.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[board-game-hunter started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[board-game-hunter has shut down successfully]=-"))
   :middleware wrap-dev})
