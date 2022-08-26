(ns game.core
  (:require [game.matrix :as M]))


(defn next-team [team]
  (case team
    :black :white
    :white :black))

(defn- in-bounds? [[w h] [x y]]
  (and (< -1 x w)
       (< -1 y h)))

(defn- occupied? [mat coord]
  (let [elem (M/get-elem mat coord)]
    (not= elem :empty)))

(defn- surrounded?
  ([mat coord path]
   (let [neighbors* (->> (M/get-neighbors* mat coord)
                         (remove (fn [[_ c]] (some? (path c))))) ; Remove already processed coords
         team       (M/get-elem mat coord)
         path'      (conj path coord)]
     (reduce
       (fn [acc [s c]]
         (if (false? (unreduced acc))
           (reduced false)                                  ; Propagate (reduced false)
           (condp = s
             :empty (reduced false)
             team   (surrounded? mat c path')
             true)))
       true
       neighbors*)))
  ([mat coord]
   (surrounded? mat coord #{})))

(defn- capture
  "Do captures on the given team's stones.
  If team is :black and their stones are surrounded, return new matrix with those :black stones removed."
  [mat coord team]
  (let [mat'    (atom mat)
        targets (->> (M/get-neighbors* mat coord)
                     (remove (fn [[t _]] (= t team)))
                     (filter (fn [[_ c]] (surrounded? mat c))))
        paths   (->> targets
                     (map (fn [[_ c]] c))
                     (map (fn [c] (M/connections* mat c)))
                     (distinct))]
    (doseq [path paths]
      (doseq [c path]
        (swap! mat' M/set-elem c :empty)))
    @mat'))


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
  (get-top [_this]
      (or (peek history)
          (M/new-matrix dim :empty)))

  (get-team [_this]
    (let [turns (count history)]
      (if (even? turns)
        :black
        :white)))

  (get-stone [this coord]
    (let [top (get-top this)]
      (M/get-elem top coord :gray)))

  (ended? [_this]
    (let [turns (count history)]
      (if (< turns 2)
        false
        (let [[bottom top] (take-last 2 history)]
          (= bottom top)))))

  (can-place? [this coord]
    (let [team (get-team this)]
      (and (in-bounds? dim coord)
           (let [top  (get-top this)
                 top' (M/set-elem top coord team)]
             (and (not (occupied? top coord))
                  (if-not (surrounded? top' coord)
                    true
                    (let [targets (->> (M/get-neighbors* top' coord)
                                       (filter (fn [[_ c]] (surrounded? top' c))))]
                      (not (empty? targets)))))))))

  (place [this coord]
    (let [top  (get-top this)
          team (get-team this)]
      (when (can-place? this coord)
        (let [top'       (M/set-elem top coord team)
              top'       (capture top' coord team)
              [bottom _] (take-last 2 history)]
          (when (not= top' bottom)                          ; Ko rule
            (let [history' (conj history top')]
              (->Game dim history')))))))

  (pass [this]
    (let [top      (get-top this)
          history' (conj history top)]
      (->Game dim history'))))