(ns kit.translation-reader.web.controllers.sentences
  (:require
    [ring.util.http-response :as http-response]
    [clojure.tools.logging :as log]
    [clojure.string :as str]
    [cheshire.core :as cheshire]
    [kit.translation-reader.web.utils.db :as db])
   (:import
    [java.util UUID]))

(defn initiate-sentences [db-conn uinfo id]
  (let [entity (db/find-one-by-keys
                db-conn
                :articles
                {:id (UUID/fromString id)})
        sentences (str/split (:original_content entity)
                             #"(?<=[.!?]|[.!?][\\'\"])(?<!e\.g\.|i\.e\.|vs\.|p\.m\.|a\.m\.|Mr\.|Mrs\.|Ms\.|St\.|Fig\.|fig\.|Jr\.|Dr\.|Prof\.|Sr\.|[A-Z]\.)\s+")]
    (db/insert-multi!
     db-conn
     :article_sentences
     [:user_id
      :article_id
      :serial_number
      :original_content
      :content]
     (map-indexed (fn [idx itm]
                    (hash-map
                     :user_id (UUID/fromString (:id uinfo))
                     :article_id (UUID/fromString id)
                     :serial_number (inc idx)
                     :original_content itm
                     :content itm))
                  sentences)))
  {})

(defn update-sentences [db-conn uinfo info]
  (db/update!
   db-conn
   :article_sentences
   info
   {:user_id (UUID/fromString (:id uinfo))
    :article_id (UUID/fromString (:article_id info))
    :lang (:lang info)
    :serial_number (:serial_number info)})
  {})

(defn delete-sentences [db-conn uinfo info]
  (db/delete!
   db-conn
   :article_sentences
   {:user_id (UUID/fromString (:id uinfo))
    :article_id (UUID/fromString (:article_id info))
    :lang (:lang info)})
  {})

