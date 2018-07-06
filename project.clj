(defproject org.nfrac/nsra "0.1.0-SNAPSHOT"
  :description "NSRA: Novelty Search / Reward, Adaptive"
  :url "https://github.com/floybix/nsra"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins [[lein-tools-deps "0.4.1"]]
  :middleware [lein-tools-deps.plugin/resolve-dependencies-with-deps-edn]
  :lein-tools-deps/config {:config-files [:install :user :project]}
  )
