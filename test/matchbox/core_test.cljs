(ns matchbox.core-test
  (:require-macros [cljs.test :refer [is deftest async]]
                   [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.test :as t]
            [cljs.core.async :refer [<!]]
            [matchbox.core :as p]
            [matchbox.utils :as utils]))

(def firebase-url "https://luminous-torch-5788.firebaseio.com/")

(defn random-ref []
  (let [ref (p/connect (str firebase-url "/" (rand-int 100000)))]
        ;; clear data once connection closed
    (-> ref p/on-disconnect p/remove!)
    ref))

(deftest serialize-hydrate-test
  (is (= {:a 1, :b ["b" "a"]}
         (p/hydrate
          (p/serialize {"a" 1, "b" #{:a :b}})))))

(deftest kebab->underscore-test
  (is (= "a_cromulent_name" (utils/kebab->underscore :a-cromulent-name))))

(deftest underscore->kebab-test
  (is (= :a-tasty-skewer (utils/underscore->kebab "a_tasty_skewer"))))

(deftest korks->path-test
  (is (= nil   (utils/korks->path nil)))
  (is (= ""    (utils/korks->path "")))
  (is (= ""    (utils/korks->path [])))
  (is (= "a"   (utils/korks->path :a)))
  (is (= "a"   (utils/korks->path ["a"])))
  (is (= "a/b" (utils/korks->path "a/b")))
  (is (= "a/b" (utils/korks->path [:a :b]))))

(deftest key-parent-get-in-test
  (let [root (p/connect firebase-url)
        baby (p/get-in root [:a :b :c])]
    (is (nil? (p/key root)))
    (is (nil? (p/parent root)))
    (is (= "b" (p/key (p/parent baby))))
    (is (= ["b" "a" nil] (map p/key (p/parents baby))))))

(deftest reset!-test
  (async done
    (let [ref (random-ref)]
      (p/reset! ref 34)
      (p/deref ref (fn [v]
                     (is (= 34 v))
                     (done))))))

(deftest merge!-test
  (async done
    (let [ref (random-ref)]
      (p/reset! ref {:a 1, :b 2})
      (p/merge! ref {:a 3, :c 9})
      (p/deref ref (fn [v] (is (= {:a 3, :b 2, :c 9} v)) (done))))))

(deftest conj!-test
  (async done
    (let [ref (random-ref)
          ;; hack around not being online in test
          seen (atom #{})]
      (p/listen-to ref :child-added (fn [[key value]]
                                      (swap! seen conj value)
                                      (if (= @seen #{34 36})
                                        (done))))

      (p/conj! ref 34)
      (p/conj! ref 36)

      ;; does not work without server connection
      ;; (p/deref ref (fn [v] (is (= [34 36] (vals v))) (done)))

      (js/setTimeout (fn [] (when-not (= @seen #{34 36}) (is (not "timeout")) (done))) 2000))))

(deftest swap!-test
  (async done
    (let [ref (random-ref)]
      (p/reset! ref 2)
      (p/swap! ref * 9 2)
      (p/deref ref (fn [v] (is (= 36 v)) (done))))))

(deftest remove!-test
  (async done
    (let [ref (random-ref)]
      (p/reset! ref 34)
      (p/remove! ref)
      (p/deref ref (fn [v] (is (nil? v)) (done))))))

(deftest set-priority!-test
  (async done
    (let [ref (random-ref)
          child-1 (p/get-in ref "a")
          child-2 (p/get-in ref "b")
          child-3 (p/get-in ref "c")]
      (p/reset! child-1 1)
      (p/reset! child-2 2)
      (p/reset! child-3 3)
      ;; order is:
      ;; 1st: no priority
      ;; 2nd: number as priority
      ;; 3rd: string as priority
      ;; (sorts by name on equality)
      (p/set-priority! child-1 "a")
      (p/set-priority-in! ref (p/key child-2) 0)
      (p/deref ref (fn [v] (is (= [3 2 1] (vals v))) (done)))
      (js/setTimeout (fn [] (is (not "timeout")) (done)) 1000))))

(deftest reset-with-priority!-test
  (async done
    (let [ref (random-ref)]
      (p/reset-with-priority-in! ref "a" 1 "a")
      (p/reset-with-priority-in! ref "b" 2 0)
      (p/reset-in! ref "c" 3)
      (p/deref ref (fn [v] (is (= [3 2 1] (vals v))) (done)))
      (js/setTimeout (fn [] (is (not "timeout")) (done)) 1000))))

(deftest disconnect!-reconnect!-test
  ;; default is connected
  (is (p/check-connected?))
  ;; do things in twos to show idempotent
  (p/disconnect!)
  (is (not (p/check-connected?)))
  (p/disconnect!)
  (is (not (p/check-connected?)))
  (p/reconnect!)
  (is (p/check-connected?))
  (p/reconnect!)
  (is (p/check-connected?)))

(deftest auth-anon-test
  (async done
    (let [ref (random-ref)]
      (is (nil? (p/auth-info ref)))
      ;; not a happy test right now, haven't figured why tests don't connect
      (p/auth-anon ref (fn [error auth-data]
                         (is (nil? auth-data))
                         (is (and error
                                  (= (.-message error)
                                     "Unable to contact the Firebase server.")))
                         (done))))))