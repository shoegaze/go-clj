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
  (let [team       (mat/get-elem board coord)
        neighbors* (mat/get-neighbors* board coord)]
    (if (= team :empty)
      false
      (reduce
        (fn [_ [t c]]
          (condp = t
            :empty (reduced false)
            team   (surrounded? board c)
            true))
        neighbors*))))

(defn- do-captures
  "Do captures on the given team's stones.
  If team is :black and their stones are surrounded, return new matrix with those :black stones removed."
  [board coord team]
  (let [board'  (atom board)
        targets (->> (mat/get-neighbors* board coord)
                     (remove (fn [[t _]] (= t team)))
                     (filter (fn [[_ c]] (surrounded? board c))))
        paths   (->> targets
                     (map (fn [[_ c]] c))
                     (map (fn [[_ c]] (mat/connections* board c)))
                     (distinct))]

    ;(println "targets*" (->> (mat/get-neighbors* board coord)))
    (println "targets" targets)
    (println "paths" paths)

    (doseq [path paths]
      (doseq [coord* path]
        (println "coord*" coord*)
        (println "team" team)

        (swap! board' mat/set-elem coord* team)))
    @board'))


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
    (or (peek history)
        (mat/new-matrix dim :empty)))

  (get-team [this]
    (let [turns (count history)]
      (if (even? turns)
        :black
        :white)))

  (get-stone [this coord]
    (mat/get-elem (get-top this) coord :gray))

  (ended? [this]
    (let [turns (count history)]
      (if (< turns 2)
        false
        (let [[bottom middle top] (take-last 3 history)]
          (= bottom middle top)))))

  ; Conditions for placement:
  ;  1. elem_xy = :empty
  ;  2.1.1 if-not (suicide? mat [x y] team) ...
  ;  2.1.2 recursive search from [x y], search for :empty
  ;  2.2.1 Do captures as mat'
  ;  2.2.2 if-not (= mat' mat) ...
  ;  2.2.2.1 then mat'
  ;  2.2.2.2 else mat
  (can-place? [this coord]
    (let [top  (get-top this)]
      (and (in-bounds? coord dim)
           (not (occupied? top coord))
           (let [team (get-team this)
                 top' (mat/set-elem top coord team)
                 top' (do-captures top' coord team)]
             (not= top' top)))))

  (place [this coord]
    (when (can-place? this coord)

      (println "placing at" coord)

      (let [team (get-team this)
            top  (get-top this)
            top' (mat/set-elem top coord team)
            top' (do-captures top' coord team)]

        (println "top" top)
        (println "top'" top')

        (->Game dim (conj history top')))))

  (pass [this]
    (let [top (get-top this)]
      (->Game dim (conj history top)))))