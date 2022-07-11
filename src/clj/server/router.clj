(ns server.router
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            ;[compojure.coercions :refer (as-int)]
            [server.pages :as pages]))


(defroutes router
           (GET "/" [] (pages/html-index))
           (route/resources "/")
           (route/not-found (pages/html-404)))