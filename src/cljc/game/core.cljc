(ns game.core
  (:require [game.matrix :as mat]))


(defn next-team [team]
  (case team
    :black :white
    :white :black))

(defn- suicide? [mat coord team]
  :TODO)

(defn- do-captures
  "Do captures on the given team's stones.
  If team is :black and their stones are surrounded, return new matrix with those :black stones removed."
  [mat coord team]
  :TODO)

(defn- can-place? [mat coord team]
  "Conditions for placement:
  1. elem_xy = :empty
  2.1.1 if (not (suicide? mat [x y] team)) ...
  2.1.2 recursive search from [x y], search for :empty
  2.2 Do captures"
  (let [elem (mat/get-elem mat coord)]
    (if (or (not= elem :empty)
            (suicide? mat coord team))
      false
      (let [team' (next-team team)]
        (do-captures mat coord team')))))


(defprotocol IGame
  (get-top [this])
  (get-team [this])
  (get-stone [this coord])
  (ended? [this])
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

  (place [this coord]
    (let [{history :history, dim :dim} this
          top   (get-top this)
          team' (next-team (get-team this))]
      (when (can-place? top coord team')
        (mat/set-elem top coord team')))))