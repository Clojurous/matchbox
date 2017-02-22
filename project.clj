(defproject matchbox "0.0.10-THOS37-1"
  :description "Firebase bindings for Clojure(Script)"
  :url "http://github.com/crisptrutski/matchbox"
  :license {:name "Eclipse Public License" :url "http://www.eclipse.org/legal/epl-v10.html"}
  :authors ["verma", "crisptrutski"]

  :dependencies
  [[org.clojure/clojure "1.8.0" :scope "provided"]
   [org.clojure/clojurescript "1.9.473" :scope "provided"]
   [org.clojure/core.async "0.3.426" :scope "provided"]
   [reagent "0.6.0" :scope "provided"]
   [frankiesardo/linked "1.2.6" :scope "provided"]
   [com.google.firebase/firebase-server-sdk "3.0.1" :exclusions [org.apache.httpcomponents/httpclient]]
   [org.apache.httpcomponents/httpclient "4.5.3"]
   [cljsjs/firebase "3.5.3-0"]
   [org.clojure/tools.namespace "0.2.11" :scope "test"]
   [doo "0.1.6"]]

  :aot [matchbox.clojure.android-stub]

  :profiles {:dev {:plugins [[lein-cljsbuild "1.1.3"]
                             [lein-doo "0.1.6"]
                             [com.jakemccrary/lein-test-refresh "0.6.0"]]}}

  :cljsbuild {:builds [{:id "test"
                        :source-paths ["src" "test"]
                        :compiler {:output-to "target/cljs/test.js"
                                   :main matchbox.runner
                                   :optimizations :none}}]})
