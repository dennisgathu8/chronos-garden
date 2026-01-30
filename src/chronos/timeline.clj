(ns chronos.timeline
  "Immutable history tracking for Chronos Garden."
  (:require [chronos.security :as security]
            [clojure.spec.alpha :as s]))

(def ^:private MAX_TIMELINE_SIZE 100000)

(s/def ::history (s/coll-of any? :kind vector?))
(s/def ::current-index int?)
(s/def ::timeline (s/keys :req-un [::history ::current-index]))

(defn create-timeline
  "Initializes a new timeline with a starting state."
  [initial-state]
  {:history [initial-state]
   :current-index 0})

(defn- log-audit
  [msg metadata]
  (binding [*out* *err*]
    (println (format "[AUDIT] %s - %s" msg (pr-str metadata)))))

(defn commit!
  "Appends a new state to the timeline. 
   If current-index is not at the end (time travel), it branches (truncates future)."
  [timeline new-state]
  (let [{:keys [history current-index]} timeline
        new-history (conj (subvec history 0 (inc current-index)) new-state)]
    (if (> (count new-history) MAX_TIMELINE_SIZE)
      (do 
        (log-audit "Circuit breaker triggered: Max timeline size reached" {})
        timeline)
      (do
        (log-audit "New state committed" {:timestamp (System/currentTimeMillis)})
        {:history new-history
         :current-index (dec (count new-history))}))))

(defn jump-to
  "Time travel to a specific index."
  [timeline index]
  (if (and (>= index 0) (< index (count (:history timeline))))
    (do
      (log-audit "Time travel jump" {:index index})
      (assoc timeline :current-index index))
    (do
      (log-audit "Invalid time travel attempt" {:index index})
      timeline)))
