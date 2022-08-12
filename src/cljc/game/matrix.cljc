(ns game.matrix)


(defn- new-row [w default]
  (->> (repeat default)
       (take w)
       (vec)))

(defn new-matrix
  ([[w h] default]
   (->> (repeat (new-row w default))
        (take h)
        (vec)))
  ([dim]
   (new-matrix dim nil)))

(defn get-elem
  ([mat [x y] default]
   (get-in mat [y x] default))
  ([mat coord]
   (get-elem mat coord nil)))

(defn set-elem [mat [x y] value]
  (assoc-in mat [y x] value))

(defn update-elem [mat [x y] update-fn]
  (let [old-value (get-elem mat x y)
        new-value (update-fn old-value)]
    (set-elem mat [x y] new-value)))

(defn get-dim [mat]
  (let [h (count mat)]
    (if (zero? h)
      [0 0]
      (let [w        (count (mat 0))
            ws       (map count mat)
            uniform? (every? #(= w %) ws)]
        (when uniform?
          [w h])))))

(defn get-row [mat y]
  (mat y))

(defn get-col [mat x]
  (map #(% x) mat))

(defn h-count [mat y group-fn group]
  (let [row (get-row mat y)
        gs  (group-by group-fn row)]
    (count (gs group))))

(defn v-count [mat x group-fn group]
  (let [col (get-col mat x)
        gs  (group-by group-fn col)]
    (count (gs group))))

(defn get-neighbors
  ([mat [x y] default]
   [(get-elem mat [(+ x 1) y      ] default)
    (get-elem mat [(- x 1) y      ] default)
    (get-elem mat [x       (+ y 1)] default)
    (get-elem mat [x       (- y 1)] default)])
  ([mat coord]
   (get-neighbors mat coord nil)))

(defn get-chunk
  ([mat [x y] dim default]
   (->> (new-matrix dim default)
        (map-indexed
          (fn [y-local row]
            (->> row
                 (map-indexed
                   (fn [x-local _]
                     (let [coord' [(+ x x-local) (+ y y-local)]
                           new-value (get-elem mat coord' default)]
                       new-value)))
                 (vec))))
        (vec)))
  ([mat coord dim]
   (get-chunk mat coord dim nil)))

(defn set-chunk [mat [x y] chk]
  (let [mat' (atom mat)
        [w-chk h-chk] (get-dim chk)]
    (doseq [y-chk (range h-chk)
            x-chk (range w-chk)
            :let [x-mat         (+ x x-chk)
                  y-mat         (+ y y-chk)
                  chk-elem      (get-elem chk x-chk y-chk)
                  [w-mat h-mat] (get-dim mat)]
            :when (and (< -1 x-mat w-mat)
                       (< -1 y-mat h-mat))]
      (swap! mat' set-elem
             x-mat
             y-mat
             chk-elem))
    @mat'))

(defn count-elem [mat value]
  (->> mat
       (flatten)
       (frequencies)
       (#(get % value 0))))

(defn has-elem? [mat value]
  (> (count-elem mat value) 0))

(defn has-pattern? [mat coord pattern]
  (let [dim   (get-dim pattern)
        chunk (get-chunk mat coord dim nil)]
    (= pattern chunk)))

(defn to-coords [mat]
  (->> mat
       (map-indexed
         (fn [y row]
           (->> row
                (map-indexed
                  (fn [x _] [x y]))
                (into []))))
       (into [])))

(defn get-neighbors* [mat coord]
  (let [neighbors  (get-neighbors mat coord)
        mat*       (to-coords mat)
        neighbors* (get-neighbors mat* coord)]
    (->> (mapv #(vector %1 %2) neighbors neighbors*)
         (filter #(not= % [nil nil]))
         (vec))))

(defn connections*
  ([mat coord path]
   (let [elem (get-elem mat coord)
         neighbors* (->> coord
                         (get-neighbors* mat)
                         (filter (fn [[elem* _]] (= elem* elem)))
                         (remove (fn [[_ coord*]] (some? (path coord*)))))
         path' (reduce (fn [path* [_ coord*]]
                         (conj path* coord*))
                       path
                       neighbors*)]
     (if (empty? neighbors*)
       path'
       (reduce
         (fn [path* [_ coord*]]
           (clojure.set/union path* (connections* mat coord* path*)))
         path'
         neighbors*))))
  ([mat coord]
   (connections* mat coord #{})))