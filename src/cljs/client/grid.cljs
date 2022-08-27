(ns client.grid
  (:require [reagent.core :as r]
            [game.core :refer (->Game, get-team)]
            [client.row :refer (row-component)]))


(defonce game (r/atom (->Game [3 3] [[]
                                     [[:empty :empty :white]
                                      [:empty :white :black]
                                      [:empty :white :empty]]])))
(defonce team (r/atom (get-team @game)))

(defn grid-component []
  [:div.grid
   (let [[_ h] (:dim @game)]
     (for [y (range h)]
       ^{:key y} [row-component game team y]))
   [:div.team (str @team " to play")]])