(ns org.nfrac.nsra
  (:require [org.nfrac.nsra.util :as util]
            [kdtree :as kdtree]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [clojure.test.check.random :as random]))

(s/def ::kd-tree
  #(instance? kdtree.Node %))

(s/def ::fitness
  (s/double-in :NaN? false))

(s/def ::character
  (s/coll-of (s/double-in :NaN? false), :min-count 1))

(s/def ::individual
  (s/keys :req [::fitness
                ::character]))

(s/def ::population
  (s/every ::individual))

(s/def ::archive
  (s/keys :req [::kd-tree]))

(s/def ::novelty-n pos-int?)

(s/def ::reward-weight (s/double-in :min 0.0 :max 1.0 :NaN? false))

(s/def ::parameters
  (s/keys :req [::novelty-n
                ::reward-weight]))


(defn novelty
  [bc archive n]
  (let [tree (::kd-tree archive)
        nns (kdtree/nearest-neighbor tree bc n)
        ds (map #(Math/sqrt (:dist-squared %)) nns)]
    (if (seq nns)
      (util/mean ds)
      1.0)))

(s/fdef novelty
        :args (s/cat :bc ::character
                     :archive ::archive
                     :n pos-int?)
        :ret (s/double-in :min 0, :NaN? false))

(defn selection
  [popn archive select-n params]
  (let [nov-n (::novelty-n params)
        fits (map ::fitness popn)
        bcs (map ::character popn)
        novs (map #(novelty % archive nov-n) bcs)
        w (::reward-weight params)
        scores (map (fn [fit nov]
                      (+ (* w fit)
                         (* (- 1.0 w) nov)))
                    (util/rank fits)
                    (util/rank novs))]
    (util/top-n-keys-by-value select-n (zipmap popn scores))))

(s/fdef selection
        :args (s/cat :popn ::population
                     :archive ::archive
                     :select-n pos-int?
                     :params ::parameters)
        :ret ::population)

(defn new-archive
  [bcs]
  {::kd-tree (kdtree/build-tree bcs)})

(s/fdef new-archive
        :args (s/cat :bcs (s/every ::character))
        :ret ::archive)

(defn update-archive
  [archive bcs]
  (let [tree (::kd-tree archive)]
    (-> archive
        (assoc ::kd-tree (reduce kdtree/insert tree bcs)))))

(s/fdef update-archive
        :args (s/cat :archive ::archive
                     :bcs (s/every ::character))
        :ret ::archive)
