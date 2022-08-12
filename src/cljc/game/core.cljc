(ns game.core
  (:require [game.matrix :as mat]))


(defn next-team [team]
  (case team
    :black :white
    :white :black))

(defn- in-bounds? [[x y] [w h]]
  (and (< -1 x w)
       (< -1 y h)))

(defn- occupied? [board coord]
  (not= (mat/get-elem board coord) :empty))

(defn surrounded? [board coord]
  (let [team  (mat/get-elem board coord)
        neighbors* (mat/get-neighbors* board coord)]
    (reduce
      (fn [[stone coord*]]
        (condp = stone
          :empty (reduced false)
          team   (surrounded? board coord*)
          true))
      neighbors*)))

(defn- do-captures
  "Do captures on the given team's stones.
  If team is :black and their stones are surrounded, return new matrix with those :black stones removed."
  [board coord team]
  (let [top'    (atom board)
        targets (->> (mat/get-neighbors* board coord)
                     (remove (fn [[team* _]] (= team* team)))
                     (filter (fn [[_ coord*]] (surrounded? board coord*))))
        paths   (->> targets
                     (map (fn [[_ coord*]] coord*))
                     (distinct))]
    (doseq [path paths]
      (doseq [coord* path]
        (println "coord*" coord*)
        (swap! top' mat/set-elem coord*)))))


(defprotocol IGame
  (get-top [this])
  (get-team [this])
  (get-stone [this coord])
  (ended? [this])
  (can-place? [this coord])
  (place [this coord]))

(defrecord Game [dim history]
  IGame
  (get-top [this]
    (or (peek history)
        (mat/new-matrix dim :empty)))

  (get-team [this]
    (let [history (:history this)
          turns   (count history)]
      (if (even? turns)
        :black
        :white)))

  (get-stone [this coord]
    (mat/get-elem (get-top this) coord :gray))

  (ended? [this]
    (let [history (:history this)
          turns   (count history)]
      (if (< turns 2)
        false
        (let [[bottom middle top] (take-last 3 history)]
          (= bottom middle top)))))

  ; Conditions for placement:
  ;  1. elem_xy = :empty
  ;  2.1.1 if (not (suicide? mat [x y] team)) ...
  ;  2.1.2 recursive search from [x y], search for :empty
  ;  2.2.1 Do captures
  ;  2.2.2
  (can-place? [this coord]
    (let [{dim :dim} this
          top  (get-top this)
          team (get-team this)]
      (and (in-bounds? coord dim)
           (not (occupied? top coord))
           (let [top' (do-captures top coord team)]
             ))))

  (place [this coord]
    (let [{dim :dim, history :history} this
          top  (get-top this)
          team (get-team this)
          top' (mat/set-elem top coord team)]
      (when (can-place? this coord)
        ; TODO: do-captures
        (->Game dim (conj history top'))))))