(ns game.core
  (:require [game.matrix :as M]))


(defn- next-team [team]
  (case team
    :black :white
    :white :black))

(defn- in-bounds? [[w h] [x y]]
  (and (< -1 x w)
       (< -1 y h)))

(defn- occupied? [mat coord]
  (let [elem (M/get-elem mat coord)]
    (not= elem :empty)))

(defn- surrounded? [mat coord]
  :TODO)

(defn- capture
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
  (place [this coord])
  (pass [this]))

(defrecord Game [dim history]
  IGame
  (get-top [this]
    (let [{dim :dim} this]
      (or (peek history)
          (M/new-matrix dim :empty))))

  (get-team [this]
    (let [history (:history this)
          turns   (count history)]
      (if (even? turns)
        :black
        :white)))

  (get-stone [this coord]
    (M/get-elem (get-top this) coord :gray))

  (ended? [this]
    (let [history (:history this)
          turns   (count history)]
      (if (< turns 2)
        false
        (let [[bottom top] (take-last 2 history)]
          (= bottom top)))))

  (can-place? [this coord]
    (let [{dim :dim} this
          team (get-team this)]
      (and (in-bounds? dim coord)
           (let [top  (get-top this)
                 top' (M/set-elem top coord team)]
             (and (not (occupied? top coord))
                  (if-not (surrounded? top' coord)
                    true
                    (let [targets (->> (M/get-neighbors* top' coord)
                                       (filter surrounded?))]
                      (not (empty? targets)))))))))

  (place [this coord]
    (let [top  (get-top this)
          team (get-team this)]
      (when (can-place? top coord)
        (let [{dim :dim, history :history} this
              top'     (M/set-elem top coord team)
              history' (conj history top')]
          (->Game dim history')))))

  (pass [this]
    (let [{history :history} this
          top      (get-top this)
          history' (conj history top)]
      (->Game dim history'))))