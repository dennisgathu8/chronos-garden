(ns chronos.growth-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [chronos.growth :as growth]
            [clojure.spec.alpha :as s]))

(defspec grow-step-property 100
  (prop/for-all [time-seed gen/int]
    (let [initial-state {:branches [{:x 300 :y 500 :angle 0 :length 50 :depth 0}]}
          result (growth/grow-step initial-state time-seed)]
      (and (s/valid? :chronos.growth/plant result)
           (vector? (:branches result))
           (>= (count (:branches result)) 1)))))

(deftest bounded-growth-test
  (testing "Growth depth is strictly bounded"
    (let [maxed-branch {:x 0 :y 0 :angle 0 :length 10 :depth 10}
          state {:branches [maxed-branch]}
          result (growth/grow-step state 123)]
      (is (= (:branches state) (:branches result))
          "A branch at max depth should not create new children"))))
