(ns user
  (:require [chronos.security :as security]
            [chronos.growth :as growth]
            [chronos.timeline :as timeline]
            [clojure.spec.alpha :as s]
            [clojure.tools.namespace.repl :refer [refresh]]))

(defn start!
  "Initializes the development environment with security policies."
  []
  (security/apply-security-policy!)
  (println "Chronos Garden: Development Environment Initialized")
  (println "Security policy applied.")

(defn status
  "Prints current status of the garden."
  []
  (println "Engine status: OK")
  (println "Security boundary: ACTIVE"))
