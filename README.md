# nsra

NSRA: Novelty Search / Reward, Adaptive.

An evolutionary algorithm that switches to directed exploration
whenever it gets stuck.

See [Conti, Madhavan, et al. 2018](https://arxiv.org/abs/1712.06560).

A Clojure implementation.


## Usage

``` clojure
(require '[org.nfrac.nsra :as nsra])
(def archive (nsra/new-archive [[0 0] [0 1]]))
(def popn
  [{:id 1
    ::nsra/fitness 1.0
    ::nsra/character [0 1.1]}
   {:id 2
    ::nsra/fitness 0.5
    ::nsra/character [0 2.0]}
  ])

;; select 1 with full weight on reward
(nsra/selection popn archive 1 {::nsra/novelty-n 2, ::nsra/reward-weight 1.0})
;; [{:id 1, ...}]

;; select 1 with full weight on novelty
(nsra/selection popn archive 1 {::nsra/novelty-n 2, ::nsra/reward-weight 0.0})
;; [{:id 2, ...}]

```


## License

Copyright © 2018 Felix Andrews

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
