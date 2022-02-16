(ns board-game-hunter.components
  (:require
    [reagent.core :as r]))

(defn text-input [{:keys [attrs subscription]
                   {:keys [on-change on-blur]} :dispatch}]
  (let [local (r/atom nil)
        value (r/track #(or @local @subscription ""))]
    (fn []
      [:label
        (:prompt attrs)
        [:input.input
          (merge attrs
            {:type :text
            :on-change #(cond->> (.. % -target -value)
                                 true      (reset! local)
                                 on-change (on-change))
            :on-blur #(if on-blur (on-blur @local))
            :value @value})]])))

(defn submit-btn [{:keys [attrs dispatch]}]
  (fn []
    [:button.button.is-primary
      {:type :button
       :on-click #(dispatch)}
      (or (:prompt attrs) "submit")]))
