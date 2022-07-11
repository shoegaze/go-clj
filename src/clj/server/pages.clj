(ns server.pages
  (:require [ring.util.response :refer (resource-response)]
            [hiccup.core :as hiccup]))


(defn html-index []
  (resource-response "index.html" {:root "public"}))

(defn html-404 []
  (hiccup/html [:h1 "404: Page Not Found"]))