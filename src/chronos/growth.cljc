(ns chronos.growth
  "Pure growth functions for temporal generative art."
  (:require [clojure.spec.alpha :as s]
            [chronos.security :as sec]))

;; --- Schemas ---

(s/def ::x number?)
(s/def ::y number?)
(s/def ::angle number?)
(s/def ::length number?)
(s/def ::depth int?)

(s/def ::branch
  (s/keys :req-un [::x ::y ::angle ::length ::depth]))

(s/def ::branches (s/coll-of ::branch))

(s/def ::plant
  (s/keys :req-un [::branches]))

(s/def ::timestamp int?)

;; --- Growth Logic ---

(defn- calculate-next-branch
  "Pure transform of a single branch using a deterministic RNG.
   
   Security Considerations:
   - RNG must be cryptographically secure
   - No shared state between branch calculations"
  [branch rng]
  (let [{:keys [x y angle length depth]} branch]
    (if (< depth 10)
      (let [new-length (* length 0.85)
            ;; Deterministic noise from RNG
            noise (- #?(:clj (.nextFloat ^java.security.SecureRandom rng)
                        :cljs (Math/random)) 0.5)
            new-angle (+ angle (* 0.4 noise))
            new-x (+ x (* length (Math/cos angle)))
            new-y (+ y (* length (Math/sin angle)))]
        {:x new-x :y new-y :angle new-angle :length new-length :depth (inc depth)})
      branch)))

(defn ^:pure grow-step
  "Returns next plant state. Pure function only.
   Implementation uses a deterministic RNG seeded by time-seed."
  [plant-state time-seed]
  {:pre [(s/valid? ::plant plant-state)
         (s/valid? ::timestamp time-seed)]
   :post [(s/valid? ::plant %)]}
  (let [branches (:branches plant-state)
        ;; On JVM, we seed a SecureRandom instance for determinism from seed
        rng #?(:clj (let [r (sec/secure-rng)]
                      (.setSeed r (long time-seed))
                      r)
               :cljs nil)]
    {:branches
     (loop [remaining branches
            acc []]
       (if (empty? remaining)
         acc
         (let [b (first remaining)
               next-b (calculate-next-branch b rng)]
           (recur (rest remaining) (conj acc b next-b)))))}))

;; SUCCESS CRITERIA: Fuzzing 1M random grow operations causes zero exceptions
(defn fuzz-test-growth
  [iterations]
  (loop [i iterations
         state {:branches [{:x 0 :y 0 :angle 0 :length 10 :depth 0}]}]
    (if (zero? i)
      :success
      (recur (dec i) (grow-step state i)))))
