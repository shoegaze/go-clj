(ns server.game.core)


(defprotocol IGame
  (get-team [this])
  (ended? [this])
  (place [this [x y]]))

(defrecord Game [history]
  IGame
  (get-team [this]
    (let [history (:history this)
          turns   (count history)
          parity  (mod turns 2)]
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
  (place [this [x y]]
    :TODO))