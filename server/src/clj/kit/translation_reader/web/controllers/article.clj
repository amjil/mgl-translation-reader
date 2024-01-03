(ns kit.translation-reader.web.controllers.article
  (:require
   [kit.translation-reader.web.utils.db :as db])
  (:import
   [java.util UUID]))

(defn get-articles
  [query-fn uinfo params]
  (let [limit (or (:limit params) 20)
        offset (or (:offset params) 0)]
    (query-fn :query-articles {:limit limit :offset offset})))

(defn get-article [db-conn uinfo id]
  (db/find-one-by-keys db-conn :articles {:id (UUID/fromString id)}))

(defn create-article [db-conn uinfo info]
  (db/insert! db-conn
              :articles
              (assoc info
                     :user_id
                     (UUID/fromString (:id uinfo))))
  {})

(defn delete-article [db-conn uinfo id]
  (db/delete! db-conn :articles 
              {:user_id (UUID/fromString (:id uinfo))
               :id (UUID/fromString id)})
  {})

(defn update-article [db-conn uinfo id info]
  (db/update! db-conn
              :articles
              info
              {:id (UUID/fromString id)
               :user_id (UUID/fromString (:id uinfo))})
  {})
