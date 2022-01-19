(ns board-game-hunter.components
  (:require
    [reagent.core :as r]))

(defn text-input [{:keys [attrs subscription dispatch]}]
  (let [local (r/atom (or @subscription ""))
        on-blur   (or (:on-blur   dispatch) (fn []))
        on-change (or (:on-change dispatch) (fn []))]
    (fn []
      [:label
        (:prompt attrs)
        [:input.input
          (merge attrs
            {:type :text
            :on-change #(->> (.. % -target -value)
                             (reset! local)
                             (on-change))
            :on-blur #(on-blur @local)
            :value @local})]])))

(defn submit-btn [{:keys [attrs dispatch]}]
  (fn []
    [:button.button.is-primary
      {:type :button
       :on-click #(dispatch)}
      (or (:prompt attrs) "submit")]))
