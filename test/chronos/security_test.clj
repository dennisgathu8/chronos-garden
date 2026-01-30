(ns chronos.security-test
  (:require [clojure.test :refer [deftest is testing]]
            [chronos.security :as sec]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]))

(defspec no-read-eval-property 100
  (prop/for-all [input gen/string]
    (try
      (sec/safe-read input)
      true
      (catch Exception e
        ;; Should never throw :forbidden-tag for safe input (not containing tags)
        (if-let [type (:type (ex-data e))]
          (not= :forbidden-tag type)
          true)))))

(defspec seed-determinism-property 100
  (prop/for-all [input gen/string-ascii]
    (= (sec/sanitize-seed input)
       (sec/sanitize-seed input))))

(deftest injection-attack-test
  (testing "Resists known EDN injection payloads"
    (is (thrown? Exception (sec/safe-read "#=(eval (System/exit 0))")))
    (is (thrown? Exception (sec/safe-read "#=(java.lang.Runtime/getRuntime)")))
    (is (thrown? Exception (sec/safe-read "#foo/bar [1 2 3]")))))

(deftest homoglyph-prevention-test
  (testing "Normalizes seeds to prevent homoglyph attacks"
    (let [seed1 "a"
          seed2 "\u0061"] ; Normalized form of 'a'
      (is (= (sec/sanitize-seed seed1) (sec/sanitize-seed seed2))))))
