(defproject clojure_httpkit "0.1.0-SNAPSHOT"
  :description "Sequence server in API comparison group"
  :url "https://bitbucket.org/mdye/api_runtime_compare"
  :license {:name "GPL v3"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [http-kit "2.1.8"]
                 [compojure "1.1.5"]
                 [com.taoensso/timbre "3.1.6"]]
  :plugins [[lein-deps-tree "0.1.2"]]
  :global-vars {*warn-on-reflection* true} ; important to improve runtime performance through type hints
  :main run
  :aot [run])
