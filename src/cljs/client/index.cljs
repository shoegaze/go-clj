(ns client.index
  (:require [reagent.dom :as rdom]
            [domina :refer (by-id)]
            [client.grid :refer (grid-component)]))


(defn ^:export init []
  (rdom/render [grid-component]
               (by-id "container")))