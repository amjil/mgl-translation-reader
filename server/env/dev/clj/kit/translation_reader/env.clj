(ns kit.translation-reader.env
  (:require
    [clojure.tools.logging :as log]
    [kit.translation-reader.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init       (fn []
                 (log/info "\n-=[translation-reader starting using the development or test profile]=-"))
   :start      (fn []
                 (log/info "\n-=[translation-reader started successfully using the development or test profile]=-"))
   :stop       (fn []
                 (log/info "\n-=[translation-reader has shut down successfully]=-"))
   :middleware wrap-dev
   :opts       {:profile       :dev
                :persist-data? true}})
