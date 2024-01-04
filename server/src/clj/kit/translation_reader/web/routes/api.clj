(ns kit.translation-reader.web.routes.api
  (:require
    [kit.translation-reader.web.controllers.health :as health]
    [kit.translation-reader.web.controllers.auth :as auth]
    [kit.translation-reader.web.controllers.article :as article]
    [kit.translation-reader.web.controllers.sentences :as sentences]
    [kit.translation-reader.web.middleware.core :as middleware]
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
   ["/articles"
    {:swagger {:tags ["article"]}
     :post {:summary "create article."
            :middleware [[middleware/wrap-restricted]]
            :parameters {:body [:map
                                [:original_lang {:optional true} string?]
                                [:original_content string?]
                                [:original_url {:optional true} string?]]}
            :responses {200 {:body any?}}
            :handler (fn [{{body :body} :parameters uinfo :identity}]
                       {:status 200 :body
                        (article/create-article (:db-conn _opts) uinfo body)})}
     :get     {:summary    "get list."
               :middleware [[middleware/wrap-restricted]]
               :parameters {:query [:map
                                    [:limit {:optional true} int?]
                                    [:offset {:optional true} int?]]}
               :responses {200 {:body any?}}
               :handler (fn [{{:keys [query]} :parameters uinfo :identity}]
                          {:status 200 :body
                           (article/get-articles (:query-fn _opts) uinfo query)})}}]
   ["/articles/:id"
    {:swagger {:tags ["article"]}
     :post {:summary "update article."
            :middleware [[middleware/wrap-restricted]]
            :parameters {:path {:id string?}
                         :body [:map
                                [:original_lang {:optional true} string?]
                                [:original_content {:optional true} string?]]}
            :responses {200 {:body any?}}
            :handler (fn [{{{id :id} :path
                            body :body} :parameters uinfo :identity}]
                       {:status 200 :body
                        (article/update-article (:db-conn _opts) uinfo id body)})}
     :get {:summary    "get article."
           :middleware [[middleware/wrap-restricted]]
           :parameters {:path {:id string?}}
           :responses {200 {:body any?}}
           :handler (fn [{{{id :id} :path} :parameters uinfo :identity}]
                      {:status 200 :body
                       (article/get-article (:db-conn _opts) uinfo id)})}
     :delete {:summary    "delete a article."
              :middleware [[middleware/wrap-restricted]]
              :parameters {:path {:id string?}}
              :responses {200 {:body any?}}
              :handler (fn [{{{id :id} :path} :parameters uinfo :identity}]
                         {:status 200 :body
                          (article/delete-article (:db-conn _opts) uinfo id)})}}]
   ["/article_sentences/initiate"
    {:swagger {:tags ["sentences"]}
     :post {:summary "initiate sentences."
            :middleware [[middleware/wrap-restricted]]
            :parameters {:body [:map
                                [:article_id string?]]}
            :responses {200 {:body any?}}
            :handler (fn [{{body :body} :parameters uinfo :identity}]
                       {:status 200 :body
                        (sentences/initiate-sentences (:db-conn _opts) uinfo body)})}}]
   ["/article_sentences"
    {:swagger {:tags ["sentences"]}
     :post {:summary "create sentences."
            :middleware [[middleware/wrap-restricted]]
            :parameters {:body [:map
                                [:serial_number  int?]
                                [:article_id  string?]
                                [:content  string?]]}
            :responses {200 {:body any?}}
            :handler (fn [{{body :body} :parameters uinfo :identity}]
                       {:status 200 :body
                        (sentences/update-sentence (:db-conn _opts) uinfo body)})}
     :get {:summary "get sentences."
           :middleware [[middleware/wrap-restricted]]
           :parameters {:query [:map
                                [:article_id  string?]]}
           :responses {200 {:body any?}}
           :handler (fn [{{{id :article_id} :query} :parameters uinfo :identity}]
                      {:status 200 :body
                       (sentences/get-article-sentences (:db-conn _opts) uinfo id)})}}]
   ["/article_sentences/delete"
    {:swagger {:tags ["sentences"]}
     :post {:summary "delete sentences."
            :middleware [[middleware/wrap-restricted]]
            :parameters {:body [:map
                                [:article_id  string?]
                                [:lang {:optional true} string?]]}
            :responses {200 {:body any?}}
            :handler (fn [{{body :body} :parameters uinfo :identity}]
                       {:status 200 :body
                        (sentences/delete-sentences (:db-conn _opts) uinfo body)})}}]
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
