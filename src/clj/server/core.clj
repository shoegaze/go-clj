(ns server.core
  (:require [ring.adapter.jetty :as jetty]
            [server.router :refer (router)]))

(defn -main [& _]
  (let [host "localhost"
        port 5055
        url  (str "http://" host ":" port)]
    (println "* Starting server on" url)
    (jetty/run-jetty router
                     {:host host
                      :port port
                      :join? true})))
