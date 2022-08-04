(ns client.row
  (:require [client.cell :refer (cell-component)]))


(defn row-component [game y]
  (let [[w h] (:dim @game)]
    [:div.row
     (for [x (range w)
           :let [key (+ (* y h) x)]]
       ^{:key key} [cell-component game [x y]])]))