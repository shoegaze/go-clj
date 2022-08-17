(ns game.core
  (:require [game.matrix :as M]))


(defn next-team [team]
  (case team
    :black :white
    :white :black))

(defn- in-bounds? [[w h] [x y]]
  (and (< -1 x w)
       (< -1 y h)))

(defn- occupied? [board coord]
  (not= (M/get-elem board coord) :empty))

(defn surrounded? [board coord]
  (let [team       (M/get-elem board coord)
        neighbors* (M/get-neighbors* board coord)]
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
        targets (->> (M/get-neighbors* board coord)
                     (remove (fn [[t _]] (= t team)))
                     (filter (fn [[_ c]] (surrounded? board c))))
        paths   (->> targets
                     (map (fn [[_ c]] c))
                     (map (fn [c] (M/connections* board c)))
                     (distinct))]

    ;(println "targets*" (->> (mat/get-neighbors* board coord)))
    (println "targets" targets)
    (println "paths" paths)

    (doseq [path paths]
      (doseq [coord* path]
        (println "coord*" coord*)
        (println "team" team)

        (swap! board' M/set-elem coord* team)))
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
        (M/new-matrix dim :empty)))

  (get-team [this]
    (let [turns (count history)]
      (if (even? turns)
        :black
        :white)))

  (get-stone [this coord]
    (M/get-elem (get-top this) coord :gray))

  (ended? [this]
    (let [turns (count history)]
      (if (< turns 2)
        false
        (let [[bottom middle top] (take-last 3 history)]
          (= bottom middle top)))))

  ; Conditions for placement:
  ; 1. coord :: [0, h) X (w, 0]
  ; 2. elem_xy = :empty
  ; 3.1. Place stone s_xy
  ; 3.2. not surrounded? s_xy
  ; 3.3.1. Do captures for current team
  ; 3.3.2. Apply Ko rule
  (can-place? [this coord]
    (let [dim (:dim this)
          top (get-top this)]
      (and (in-bounds? dim coord)
           (not (occupied? top coord))
           (let [team (get-team this)
                 top' (M/set-elem top coord team)]
             (if (surrounded? top' coord)
               false
               (let [top' (do-captures top' coord team)]
                 (not= top' top)))))))

  (place [this coord]
    (when (can-place? this coord)

      (println "placing at" coord)

      (let [team (get-team this)
            top  (get-top this)
            top' (M/set-elem top coord team)
            top' (do-captures top' coord team)]

        (println "top" top)
        (println "top'" top')

        (->Game dim (conj history top')))))

  (pass [this]
    (let [top (get-top this)]
      (->Game dim (conj history top)))))