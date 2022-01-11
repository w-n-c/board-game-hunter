(ns board-game-hunter.app
  (:require [board-game-hunter.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
