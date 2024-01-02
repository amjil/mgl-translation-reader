(ns kit.translation-reader.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init       (fn []
                 (log/info "\n-=[translation-reader starting]=-"))
   :start      (fn []
                 (log/info "\n-=[translation-reader started successfully]=-"))
   :stop       (fn []
                 (log/info "\n-=[translation-reader has shut down successfully]=-"))
   :middleware (fn [handler _] handler)
   :opts       {:profile :prod}})
