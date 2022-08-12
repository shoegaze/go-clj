(ns client.cell
  (:require [game.core :refer (->Game, get-stone, get-top, next-team, place)]))


(defn- cell-fg-img-class [game _team coord]
  (let [stone (get-stone game coord)]
    (case stone
      :black ["stone" "black"]
      :white ["stone" "white"]
      :gray  ["stone" "error"]
      :empty ["stone" "empty"])))

(defn- cell-fg-img [game team coord]
  (let [class (cell-fg-img-class @game @team coord)]
    [:img.cell-fg {:class class
                   :on-click #(do (swap! game place coord)
                                  (swap! team next-team))}]))

(defn- cell-bg-img-class [[w h] [x y]]
  (let [left?   (= x 0)
        right?  (= x (- w 1))
        top?    (= y 0)
        bottom? (= y (- h 1))
        corner? (or (and left? top?)
                    (and left? bottom?)
                    (and right? top?)
                    (and right? bottom?))
        edge?   (and (not corner?)
                     (or left? right? top? bottom?))
        inner?  (and (not corner?)
                     (not edge?))]
    [(when left?   "left")
     (when right?  "right")
     (when top?    "top")
     (when bottom? "bottom")
     (when corner? "corner")
     (when edge?   "edge")
     (when inner?  "inner")]))

(defn- cell-bg-img [game _team coord]
  (let [dim   (:dim @game)
        class (cell-bg-img-class dim coord)]
    [:img.cell-bg {:class class}]))

(defn cell-component [game team coord]
  [:span.cell
   [cell-fg-img game team coord]
   [cell-bg-img game team coord]])