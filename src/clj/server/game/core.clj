(ns server.game.core
  (:require [server.game.matrix :as mat]))


(defprotocol IGame
  (get-team [this])
  (ended? [this])
  (place [this coord]))

(defn- next-team [team]
  (condp = team
    :black :white
    :white :black
    nil))

(defn- can-place? [mat [x y] team]
  ; 1. elem_xy = :empty
  ; 2.1 Do captures
  ; 2.2.1 if (not (suicide? mat [x y] team)) ...
  ; 2.2.2 Recursive search from [x y], search for :empty
  )

(defrecord Game [dim history]
  IGame
  (get-team [this]
    (let [history (:history this)
          turns  (count history)
          parity (mod turns 2)]
      (condp = parity
        0 :black
        1 :white)))

  (ended? [this]
    (let [history (:history this)
          turns   (count history)]
      (if (< turns 2)
        false
        (let [[left right] (take-last 2 history)]
          (= left right)))))

  (place [this coord]
    (let [{history :history, dim :dim} this
          top   (or (take-last 1 history)
                    (mat/new-matrix dim))
          team' (next-team (get-team this))]
      (when (can-place? top coord team')
        (mat/set-elem top coord team')))))