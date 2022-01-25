(ns board-game-hunter.routes.app
  (:require
    #?@(:clj [[board-game-hunter.layout :as layout]
              [board-game-hunter.middleware :as middleware]
              [ring.util.http-response :as response]
              [clojure.java.io :as io]])))
#?(:clj
   (defn home-page [request]
     (layout/render request "index.html")))

(defn app-routes []
  [""
   #?(:clj {:middleware [middleware/wrap-csrf middleware/wrap-formats]
            :get        home-page})
   ["/" :home]
   ["/about" :about]
   ["/docs" #?(:clj {:get (fn [_]
                            (-> (response/ok (-> "docs/docs.md" io/resource slurp))
                                (response/header "Content-Type" "text/plain; charset=utf-8")))})]])
