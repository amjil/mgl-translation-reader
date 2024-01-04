(ns kit.translation-reader.web.controllers.sentences
  (:require
    [ring.util.http-response :as http-response]
    [clojure.tools.logging :as log]
    [clojure.string :as str]
    [cheshire.core :as cheshire]
    [kit.translation-reader.web.utils.db :as db])
   (:import
    [java.util UUID]))

(defn initiate-sentences [db-conn uinfo body]
  (let [entity (db/find-by-keys
                db-conn
                :article_sentences
                {:article_id (UUID/fromString (:article_id body))
                 :user_id (UUID/fromString (:id uinfo))})]
    (when (empty? entity)
      (let [entity (db/find-one-by-keys
                    db-conn
                    :articles
                    {:id (UUID/fromString (:article_id body))})
            sentences
        ;; (str/split
        ;;  (:original_content entity)
        ;;  #"(?<=[.!?]|[.!?][\\'\"])(?<!e\.g\.|i\.e\.|vs\.|p\.m\.|a\.m\.|Mr\.|Mrs\.|Ms\.|St\.|Fig\.|fig\.|Jr\.|Dr\.|Prof\.|Sr\.|[A-Z]\.)\s+")
            (str/split-lines (:original_content entity))]
        (db/insert-multi!
         db-conn
         :article_sentences
         [:user_id
          :article_id
          :serial_number
          :original_content
          :content]
         (map-indexed (fn [idx itm]
                        [(UUID/fromString (:id uinfo))
                         (UUID/fromString (:article_id body))
                         idx
                         itm
                         itm])
                      sentences)))))
  
  {})

(defn update-sentence [db-conn uinfo info]
  (db/update!
   db-conn
   :article_sentences
   (assoc info :article_id (UUID/fromString (:article_id info)))
   {:user_id (UUID/fromString (:id uinfo))
    :article_id (UUID/fromString (:article_id info))
    ;; :lang (:lang info)
    :serial_number (:serial_number info)})
  {})

(defn delete-sentences [db-conn uinfo info]
  (db/delete!
   db-conn
   :article_sentences
   {:user_id (UUID/fromString (:id uinfo))
    :article_id (UUID/fromString (:article_id info))
    ;; :lang (:lang info)
    })
  {})

(defn get-article-sentences [db-conn uinfo id]
  (db/find-by-keys db-conn
                   :article_sentences
                   {:user_id (UUID/fromString (:id uinfo))
                    :article_id (UUID/fromString id)}
                   {:order-by [[:serial_number :asc]]}))

