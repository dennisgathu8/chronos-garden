(ns chronos.health
  (:require [clojure.java.io :as io])
  (:import [java.net URI]
           [java.net.http HttpClient HttpRequest HttpRequest$Builder HttpResponse$BodyHandlers]
           [java.time Duration]))

(defn -main [& args]
  (try
    (let [client (-> (HttpClient/newBuilder)
                     (.connectTimeout (Duration/ofSeconds 2))
                     (.build))
          request (-> (HttpRequest/newBuilder)
                      (.uri (URI/create "http://localhost:8080/"))
                      (.timeout (Duration/ofSeconds 2))
                      (.build))
          response (.send client request (HttpResponse$BodyHandlers/ofString))]
      (if (= 200 (.statusCode response))
        (System/exit 0)
        (System/exit 1)))
    (catch Exception e
      (.printStackTrace e)
      (System/exit 1))))
