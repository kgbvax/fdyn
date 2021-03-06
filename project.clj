(defproject tinymasq "0.1.2-ffms-3"
  :description "A DynDNS server for local services"
  :url "https://github.com/kgbvax/fdyn"
  :license {:name "Apache License, Version 2.0" :url "http://www.apache.org/licenses/LICENSE-2.0.html"}
  :dependencies [
                 [org.clojure/clojure "1.6.0"]
                 [com.taoensso/carmine "2.12.2"]
                 [dnsjava/dnsjava "2.1.7"]
                 [com.taoensso/timbre "3.3.1"]
                 [org.clojure/core.incubator "0.1.3"]
                 [org.clojure/core.cache "0.6.4"]
                 [org.clojure/core.async "0.2.374"]
                 [ring/ring-ssl "0.2.1"]
                 [ring-middleware-format "0.7.0"]
                 [ring/ring-jetty-adapter "1.4.0"]
                 [com.cemerick/friend "0.2.1"]
                 [ring "1.4.0"]
                 [compojure "1.4.0" :exclusions [ring/ring-core]]
                 [clj-time "0.11.0"]
                 ;;      [org.clojure/data.json "0.2.6"]
                 [hiccup "1.0.5"]
                 [compojure "1.1.6"]]

  :exclusions [org.clojure/clojure org.clojure/core.cache]
  :profiles {
             :dev {
                   :dependencies [[midje "1.8.3"]]
                   :plugins      [[lein-set-version "0.3.0"]
                                  [lein-midje "3.1.3"]
                                  [lein-ancient "0.5.5"]
                                  [lein-tar "2.0.0"]
                                  [lein-tag "0.1.0"]
                                  [lein-ubersource "0.1.1"]
                                  [lein-ring "0.9.7"]]
                   }
             }

  :set-version {
                :updates [{:path "src/tinymasq/core.clj" :search-regex #"\"\d+\.\d+\.\d+\""}
                          {:path "README.md" :no-snapshot true}]
                }
  :ring {:handler hello-world.core/handler}

  :aliases {
            "genhash" ["run" "-m" "tinymasq.genhash"]
            }

  :aot [tinymasq.core tinymasq.store tinymasq.api tinymasq.config]

  :main tinymasq.core
  :jvm-opts ["-Xmx100m"]

  )
