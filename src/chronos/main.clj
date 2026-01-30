(ns chronos.main
  (:gen-class)
  (:require [ring.adapter.jetty :as jetty]
            [chronos.security :as sec]
            [clojure.java.io :as io]
            [clojure.string :as str]))

(defn app [req]
  {:status 200
   :headers {"Content-Type" "text/plain"
             "Content-Security-Policy" "default-src 'self'; script-src 'self' https://cdn.tailwindcss.com; style-src 'self' 'unsafe-inline';"}
   :body "Chronos Garden: Temporal Generative Engine Active"})

(defn load-secret
  "Reads secret from file system (Docker secrets / k8s secrets).
   Returns trimmed string or throws on missing."
  [secret-name]
  (let [path (str (System/getenv "SECRETS_PATH") "/" secret-name)]
    (if (.exists (io/file path))
      (-> (slurp path) str/trim)
      (throw (ex-info "Secret not mounted" {:secret secret-name})))))

(defn -main [& args]
  (sec/apply-security-policy!)
  (println "Starting Chronos Garden on port 8080...")
  (jetty/run-jetty app {:port 8080 :join? true}))
