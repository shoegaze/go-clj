(ns client.grid
  (:require [reagent.core :as r]
            [game.core :refer (->Game)]
            [client.row :refer (row-component)]))


(defonce game (r/atom (->Game [9 9] [])))
(defonce team (r/atom :black))

(defn grid-component []
  [:div.grid
   (let [[_ h] (:dim @game)]
     (for [y (range h)]
       ^{:key y} [row-component game team y]))])