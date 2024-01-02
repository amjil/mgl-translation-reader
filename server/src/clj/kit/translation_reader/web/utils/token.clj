(ns kit.translation-reader.web.utils.token
  (:require
   [buddy.core.codecs :as codecs]
   [buddy.core.hash :as hash]
   [buddy.sign.jwt :as jwt :refer [encrypt decrypt]]
   [java-time :as time]
   [clojure.string :as str]))

(defn generate-token []
  (-> (hash/sha256 (.toString (java.util.UUID/randomUUID)))
      (codecs/bytes->hex)))

(defn jwt-token [secret info]
  (let [uuid   (str/replace (.toString (java.util.UUID/randomUUID)) #"-" "")
        exp    (-> (time/plus (time/zoned-date-time) (time/days 90))
                   time/instant
                   time/to-millis-from-epoch)
        claims (merge {:jti uuid
                       :exp exp}
                      info)]
    (encrypt claims (hash/sha256 secret)
             {:alg :a256kw
              :enc :a128gcm})))

(defn decrypt-token [secret token]
  (decrypt token (hash/sha256 secret)
           {:alg :a256kw
            :enc :a128gcm}))
