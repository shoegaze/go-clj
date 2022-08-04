(ns client.cell)


(defn- cell-img-class [[w h] [x y]]
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

(defn- cell-img [game coord]
  (let [dim   (:dim @game)
        class (cell-img-class dim coord)]
    [:img {:class class}]))

(defn cell-component [game coord]
  [:span.cell
   [cell-img game coord]])