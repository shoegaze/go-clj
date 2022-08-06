(ns game.core
  (:require [game.matrix :as mat]))


(defn next-team [team]
  (case team
    :black :white
    :white :black))

(defn- coords-bounded? [[x y] [w h]]
  (and (< -1 x w)
       (< -1 y h)))

(defn- suicide? [mat coord team]
  :TODO)

(defn- do-captures
  "Do captures on the given team's stones.
  If team is :black and their stones are surrounded, return new matrix with those :black stones removed."
  [mat coord team]
  :TODO)


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
        (let [[bottom top] (take-last 2 history)]
          (= bottom top)))))

  ; Conditions for placement:
  ;  1. (x,y) :: (w,0] x [0,h)
  ;  2. elem_xy = :empty
  ;  3.1.1 if (not (suicide? mat [x y] team)) ...
  ;  3.1.2 recursive search from [x y], search for :empty
  ;  3.2 Do captures
  (can-place? [this coord]
    (let [{dim :dim} this
          top  (get-top this)
          team (get-team this)]
      (and (coords-bounded? coord dim)
           (= (get-stone this coord) :empty)
           ;(if (not (suicide? top [x y] team))
           ;  then
           ;  else)
           )))

  (place [this coord]
    (let [{history :history, dim :dim} this
          top  (get-top this)
          team (get-team this)]
      (when (can-place? this coord)
        (let [board'   (mat/set-elem top coord team)
              history' (conj history board')]
          (->Game dim history'))))))