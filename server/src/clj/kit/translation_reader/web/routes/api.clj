(ns kit.translation-reader.web.routes.api
  (:require
    [kit.translation-reader.web.controllers.health :as health]
    [kit.translation-reader.web.controllers.auth :as auth]
    [kit.translation-reader.web.middleware.exception :as exception]
    [kit.translation-reader.web.middleware.formats :as formats]
    [integrant.core :as ig]
    [reitit.coercion.malli :as malli]
    [reitit.ring.coercion :as coercion]
    [reitit.ring.middleware.muuntaja :as muuntaja]
    [reitit.ring.middleware.parameters :as parameters]
    [reitit.swagger :as swagger]))

(def route-data
  {:coercion   malli/coercion
   :muuntaja   formats/instance
   :swagger    {:id ::api}
   :middleware [;; query-params & form-params
                parameters/parameters-middleware
                  ;; content-negotiation
                muuntaja/format-negotiate-middleware
                  ;; encoding response body
                muuntaja/format-response-middleware
                  ;; exception handling
                coercion/coerce-exceptions-middleware
                  ;; decoding request body
                muuntaja/format-request-middleware
                  ;; coercing response bodys
                coercion/coerce-response-middleware
                  ;; coercing request parameters
                coercion/coerce-request-middleware
                  ;; exception handling
                exception/wrap-exception]})

;; Routes
(defn api-routes [_opts]
  [["/swagger.json"
    {:get {:no-doc  true
           :swagger {:info {:title "kit.translation-reader API"}}
           :handler (swagger/create-swagger-handler)}}]
   ["/health"
    {:get health/healthcheck!}]
  ;;  ["/articles"
  ;;   {:swagger {:tags ["article"]}
  ;;    :get     {:summary    "get list."
  ;;              :middleware [[middleware/wrap-restricted]]
  ;;              :parameters {:query {(ds/opt :page)    int?
  ;;                                   (ds/opt :perpage) int?}}
  ;;              :responses  {200 {:body {:code            int?
  ;;                                       :msg             string?
  ;;                                       (ds/opt :errors) any?
  ;;                                       (ds/opt :data)   any?}}}
  ;;              :handler    (fn [{token           :identity
  ;;                                {:keys [query]} :parameters}]
  ;;                            (ok (service/get-models token query)))}}]
   ["/auth"
    {:swagger {:tags ["auth"]}}
    ["/login"
     {:post {:summary "sign in."
             :parameters {:body {:email string?
                                 :password string?}}
             :responses {200 {:body any?}}
             :handler (fn [{{:keys [body]} :parameters headers :headers addr :remote-addr}]
                        {:status 200 :body
                         (auth/login (:db-conn _opts) (:token-secret _opts) body)})}}]
    ["/signup"
     {:post {:summary "sign up."
             :parameters {:body {:email string?
                                 :password string?}}
             :responses {200 {:body {:token string?}}}
             :handler (fn [{{:keys [body]} :parameters headers :headers addr :remote-addr}]
                        {:status 200 :body
                         (auth/signup (:db-conn _opts) (:token-secret _opts) body)})}}]]])

(derive :reitit.routes/api :reitit/routes)

(defmethod ig/init-key :reitit.routes/api
  [_ {:keys [base-path]
      :or   {base-path ""}
      :as   opts}]
  [base-path route-data (api-routes opts)])
