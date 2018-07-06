(ns org.nfrac.nsra.util
  (:require [clojure.test.check.random :as random]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen])
  (:refer-clojure :exclude [rand rand-int rand-nth shuffle]))

(s/def ::rng (-> #(satisfies? random/IRandom %)
                 (s/with-gen #(gen/fmap random/make-random (gen/int)))))

(defn rand
  (^double [rng ^double upper]
   (-> (random/rand-double rng)
       (* upper)))
  (^double [rng ^double lower ^double upper]
   {:pre [(<= lower upper)]}
   (-> (random/rand-double rng)
       (* (- upper lower))
       (+ lower))))

(defn rand-int
  "Uniform integer between lower (inclusive) and upper (exclusive)."
  (^long [rng ^long upper]
   (-> (random/rand-double rng)
       (* upper)
       (Math/floor)
       (long)))
  (^long [rng ^long lower ^long upper]
   (-> (random/rand-double rng)
       (* (- upper lower))
       (+ lower)
       (Math/floor)
       (long))))

(defn rand-nth
  [rng xs]
  (nth xs (rand-int rng (count xs))))

;; copied from
;; https://github.com/clojure/data.generators/blob/bf2eb5288fb59045041aec01628a7f53104d84ca/src/main/clojure/clojure/data/generators.clj
;; adapted to splittable RNG

(defn ^:private fisher-yates
  "http://en.wikipedia.org/wiki/Fisher–Yates_shuffle#The_modern_algorithm"
  [rng coll]
  (let [as (object-array coll)]
    (loop [i (dec (count as))
           r rng]
      (if (<= 1 i)
        (let [[r1 r2] (random/split r)
              j (rand-int r1 (inc i))
              t (aget as i)]
          (aset as i (aget as j))
          (aset as j t)
          (recur (dec i) r2))
        (into (empty coll) (seq as))))))

(defn shuffle
  [rng coll]
  (fisher-yates rng coll))

(defn top-n-keys-by-value
  "Like `(reverse (take n (keys (sort-by val > m))))` but faster."
  [n m]
  (cond
   (<= n 0) []
   (empty? m) []
   (== n 1) [(key (apply max-key val (seq m)))]
   :else
   (loop [ms (seq m)
          am (sorted-map-by #(compare [(m %1) %1] [(m %2) %2]))
          curr-min -1.0]
     (if (empty? ms)
       (keys am)
       (let [[k v] (first ms)]
         (cond
          ;; just initialising the set
          (empty? am)
          (recur (next ms)
                 (assoc am k v)
                 (double v))
          ;; filling up the set
          (< (count am) n)
          (recur (next ms)
                 (assoc am k v)
                 (double (min curr-min v)))
          ;; include this one, dominates previous min
          (> v curr-min)
          (let [new-am (-> (dissoc am (first (keys am)))
                           (assoc k v))]
            (recur (next ms)
                   new-am
                   (double (first (vals new-am)))))
          ;; exclude this one
          :else
          (recur (next ms) am curr-min)))))))

(defn mean
  [xs]
  (-> (reduce + 0 xs)
      (/ (count xs))))

(defn rank
  "Returns a sequence of rank scores corresponding to xs. Ordering of
  ranks is same as original data: the largest value will have rank N
  and the smallest rank 1. Ties are averaged.

  Example:
  (rank [20 40 10 10 30 30 30])
  ;; --> [3 7 1.5 1.5 5 5 5]
  "
  ([xs]
   (rank xs <))
  ([xs comp]
   (let [ixs (sort-by peek comp (map vector (range) xs))]
     (loop [ix-groups (partition-by peek ixs)
            orank 0
            v-out (transient xs)]
       (if-let [group (first ix-groups)]
         (let [ranks (range (+ orank 1) (+ orank 1 (count group)))
               rank (mean ranks)]
           (recur (rest ix-groups)
                  (+ orank (count group))
                  (reduce (fn [v [i x]]
                            (assoc! v i rank))
                          v-out
                          group)))
         ;; done
         (persistent! v-out))))))
