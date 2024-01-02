(ns kit.translation-reader.web.utils.db
  (:require
   [next.jdbc :as jdbc]
   [next.jdbc.sql :as sql]
   [next.jdbc.result-set :as rs]
   [next.jdbc.prepare]
   [next.jdbc.date-time]
   [cheshire.core :refer [generate-string parse-string]])
  (:import
   [java.sql Array]
   [org.postgresql.util PGobject]))

(defn update! [conn t w s]
  (sql/update! conn t w s
               {:builder-fn rs/as-unqualified-lower-maps}))

(defn get-by-id [conn t id]
  (sql/get-by-id conn t id
                 {:builder-fn rs/as-unqualified-lower-maps}))

(defn find-by-keys
  ([conn t w]
   (sql/find-by-keys conn t w
                     {:builder-fn rs/as-unqualified-lower-maps}))
  ([conn t w ex]
   (sql/find-by-keys conn t w
                     (merge
                      {:builder-fn rs/as-unqualified-lower-maps}
                      ex))))

(defn find-one-by-keys
  ([conn t w]
   (first
    (sql/find-by-keys conn t w
                      {:builder-fn rs/as-unqualified-lower-maps})))
  ([conn t w ex]
   (first
    (sql/find-by-keys conn t w
                      (merge
                       {:builder-fn rs/as-unqualified-lower-maps}
                       ex)))))

(defn insert! 
  ([conn t info]
   (sql/insert! conn t info
                {:builder-fn rs/as-unqualified-lower-maps}))
  ([conn t info opt]
   (sql/insert! conn t info 
                (merge 
                 {:builder-fn rs/as-unqualified-lower-maps}
                 opt))))

(defn insert-multi! 
  ([conn t cols list]
   (sql/insert-multi! conn t cols list
                      {:builder-fn rs/as-unqualified-lower-maps}))
  ([conn t cols list opt]
   (sql/insert-multi! conn t cols list
                      (merge
                       {:builder-fn rs/as-unqualified-lower-maps}
                       opt))))

(defn delete! [conn t w]
  (sql/delete! conn t w))

(defn execute!
  ([conn sqlmap]
   (jdbc/execute! conn sqlmap
                  {:builder-fn rs/as-unqualified-lower-maps}))
  ([conn sqlmap opt]
   (jdbc/execute! conn sqlmap
                  (merge
                   {:builder-fn rs/as-unqualified-lower-maps}
                   opt))))


;; ---------
(defn pgobj->clj [^org.postgresql.util.PGobject pgobj]
  (let [type (.getType pgobj)
        value (.getValue pgobj)]
    (case type
      "json" (parse-string value true)
      "jsonb" (parse-string value true)
      "citext" (str value)
      value)))

(extend-protocol next.jdbc.result-set/ReadableColumn
  java.sql.Timestamp
  (read-column-by-label [^java.sql.Timestamp v _]
    (.toLocalDateTime v))
  (read-column-by-index [^java.sql.Timestamp v _2 _3]
    (.toLocalDateTime v))
  java.sql.Date
  (read-column-by-label [^java.sql.Date v _]
    (.toLocalDate v))
  (read-column-by-index [^java.sql.Date v _2 _3]
    (.toLocalDate v))
  java.sql.Time
  (read-column-by-label [^java.sql.Time v _]
    (.toLocalTime v))
  (read-column-by-index [^java.sql.Time v _2 _3]
    (.toLocalTime v))
  java.sql.Array
  (read-column-by-label [^java.sql.Array v _]
    (vec (.getArray v)))
  (read-column-by-index [^java.sql.Array v _2 _3]
    (vec (.getArray v)))
  org.postgresql.util.PGobject
  (read-column-by-label [^org.postgresql.util.PGobject pgobj _]
    (pgobj->clj pgobj))
  (read-column-by-index [^org.postgresql.util.PGobject pgobj _2 _3]
    (pgobj->clj pgobj)))

(defn clj->jsonb-pgobj [value]
  (doto (PGobject.)
    (.setType "jsonb")
    (.setValue (generate-string value))))

(extend-protocol next.jdbc.prepare/SettableParameter
  clojure.lang.IPersistentMap
  (set-parameter [^clojure.lang.IPersistentMap v ^java.sql.PreparedStatement stmt ^long idx]
    (.setObject stmt idx (clj->jsonb-pgobj v)))
  clojure.lang.IPersistentVector
  (set-parameter [^clojure.lang.IPersistentVector v ^java.sql.PreparedStatement stmt ^long idx]
    (let [conn      (.getConnection stmt)
          meta      (.getParameterMetaData stmt)
          type-name (.getParameterTypeName meta idx)]
      (if-let [elem-type (when (= (first type-name) \_)
                           (apply str (rest type-name)))]
        (.setObject stmt idx (.createArrayOf conn elem-type (to-array v)))
        (.setObject stmt idx (clj->jsonb-pgobj v))))))