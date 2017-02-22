(set-env!
 :dependencies
 '[[org.clojure/clojure "1.8.0" :scope "provided"]
   [org.clojure/clojurescript "1.9.473" :scope "provided"]
   ;; packaged dependencies
   [com.google.firebase/firebase-server-sdk "3.0.3" :exclusions [org.apache.httpcomponents/httpclient]]
   [cljsjs/firebase "3.5.3-0"]
   [org.apache.httpcomponents/httpclient "4.5.3"]
   ;; optional namespace dependencies
   [org.clojure/core.async "0.3.426" :scope "provided"]
   [reagent "0.6.0" :scope "provided"]
   [frankiesardo/linked "1.2.9" :scope "provided"]
   ;; build tooling
   [adzerk/boot-cljs "1.7.228-2" :scope "test"]
   [adzerk/bootlaces "0.1.13" :scope "test"]
   [adzerk/boot-test "1.2.0" :scope "test"]
   [crisptrutski/boot-cljs-test "0.3.0" :scope "test"]]
 :source-paths #{"src"})

(require
  '[adzerk.bootlaces :refer :all]
  '[adzerk.boot-cljs :refer :all]
  '[adzerk.boot-test :refer :all]
  '[crisptrutski.boot-cljs-test :refer [test-cljs]])

(def +version+ "0.0.10-THOS37-1")
(bootlaces! +version+)

(task-options!
  pom {:project 'matchbox
       :version +version+
       :description "Firebase bindings for Clojure(Script)"
       :url "http://github.com/crisptrutski/matchbox"
       :scm {:url "http://github.com/crisptrutski/matchbox"}}
  aot {:namespace #{'matchbox.clojure.android-stub}}
  test-cljs {:js-env :phantom})

(deftask deps [] identity)

(deftask testing []
  (merge-env! :source-paths #{"test"})
  identity)

(deftask watch-js [] (comp (testing) (watch) (test-cljs)))

(deftask watch-jvm [] (comp (aot) (testing) (watch) (test)))

(deftask ci []
  (task-options!
    test {:junit-output-to "junit-out"}
    test-cljs {:exit? true})
  (comp (aot) (testing) (test) (test-cljs)))

(deftask build []
  (comp (pom) (aot) (jar)))
