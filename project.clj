(defproject luchiniatwork/terra "0.1.0"
  :description "Write Terraform configurations in pure Clojure."
  :url "http://github.com/luchiniatwork/terra"

  :license {:name "MIT License"
            :url "https://opensource.org/licenses/MIT"}

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [cheshire "5.8.0"]]

  :main ^:skip-aot terra.core

  :target-path "target/%s"

  :profiles {:uberjar {:aot :all}})
