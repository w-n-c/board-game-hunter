(ns board-game-hunter.middleware
  (:require
    [board-game-hunter.env :refer [defaults]]
    [clojure.tools.logging :as log]
    [board-game-hunter.layout :refer [error-page]]
    [ring.middleware.anti-forgery :refer [wrap-anti-forgery]]
    [board-game-hunter.middleware.formats :as formats]
    [muuntaja.middleware :refer [wrap-format wrap-params]]
    [board-game-hunter.config :refer [env]]
    [ring-ttl-session.core :refer [ttl-memory-store]]
    [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
    [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]
            [buddy.auth.accessrules :refer [restrict]]
            [buddy.auth :refer [authenticated?]]
    [buddy.auth.backends.token :refer [jwe-backend]]
            [buddy.sign.jwt :refer [encrypt]]
            [buddy.core.nonce :refer [random-bytes]][buddy.sign.util :refer [to-timestamp]])
   (:import
    [java.util Calendar Date]
    ))

(defn wrap-internal-error [handler]
  (fn [req]
    (try
      (handler req)
      (catch Throwable t
        (log/error t (.getMessage t))
        (error-page {:status 500
                     :title "Something very bad has happened!"
                     :message "We've dispatched a team of highly trained gnomes to take care of the problem."})))))

(defn wrap-csrf [handler]
  (wrap-anti-forgery
    handler
    {:error-response
     (error-page
       {:status 403
        :title "Invalid anti-forgery token"})}))


(defn wrap-formats [handler]
  (let [wrapped (-> handler wrap-params (wrap-format formats/instance))]
    (fn [request]
      ;; disable wrap-formats for websockets
      ;; since they're not compatible with this middleware
      ((if (:websocket? request) handler wrapped) request))))

(defn on-error [request response]
  (error-page
    {:status 403
     :title (str "Access to " (:uri request) " is not authorized")}))

(defn wrap-restricted [handler]
  (restrict handler {:handler authenticated?
                     :on-error on-error}))

(def secret (random-bytes 32))

(def token-backend
  (jwe-backend {:secret secret
                :options {:alg :a256kw
                          :enc :a128gcm}}))

(defn token [username]
  (let [claims {:user (keyword username)
                :exp (to-timestamp
                       (.getTime
                         (doto (Calendar/getInstance)
                           (.setTime (Date.))
                           (.add Calendar/HOUR_OF_DAY 1))))}]
    (encrypt claims secret {:alg :a256kw :enc :a128gcm})))

(defn wrap-auth [handler]
  (let [backend token-backend]
    (-> handler
        (wrap-authentication backend)
        (wrap-authorization backend))))

(defn wrap-base [handler]
  (-> ((:middleware defaults) handler)
      wrap-auth
      (wrap-defaults
        (-> site-defaults
            (assoc-in [:security :anti-forgery] false)
            (assoc-in  [:session :store] (ttl-memory-store (* 60 30)))))
      wrap-internal-error))
