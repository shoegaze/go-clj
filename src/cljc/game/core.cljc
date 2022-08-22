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

(defn- surrounded?-inner [board coord]
  (let [team       (M/get-elem board coord)
        neighbors* (M/get-neighbors* board coord)]
    (reduce
      (fn [acc [t c]]
        (println " * acc" acc)
        (println " * t" t)
        (println " * c" c)

        (condp = t
          :empty (reduced false)
          team   (surrounded?-inner board c)
          true))
      neighbors*)))

(defn surrounded? [board coord]
  (let [team (M/get-elem board coord)]
    (if (= team :empty)
      false
      (surrounded?-inner board coord))))

(defn- do-captures
  "Do captures on the given team's stones.
  If team is :black and their stones are surrounded, return new matrix with those :black stones removed."
  [board coord team]
  (let [board'  (atom board)
        targets (->> (M/get-neighbors* board coord)         ; TODO: get all neighbors of coord's connected path
                     (remove (fn [[t _]] (= t team)))
                     (filter (fn [[_ c]] (surrounded? board c))))
        paths   (->> targets
                     (map (fn [[_ c]] c))
                     (map (fn [c] (M/connections* board c)))
                     (distinct))]

    (println " targets*" (->> (M/get-neighbors* board coord)))
    (println " targets" targets)
    (println " paths" paths)

    (doseq [path paths]
      (do
        (println "  path" path)

        (doseq [coord* path]
          (do
            (println "   coord*" coord*)
            (println "   team" team)

            (swap! board' M/set-elem coord* :empty)))))
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

  (can-place? [this coord]
    (let [{dim :dim} this
          team (get-team this)]
      (and (in-bounds? dim coord)
           (let [top  (get-top this)
                 top' (M/set-elem top coord team)]
             (not (occupied? top coord))
             (if-not (surrounded? top' coord)
               true
               (let [targets (->> (M/get-neighbors* top' coord)
                                  (filter surrounded?))]
                 (not (empty? targets))))))))

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