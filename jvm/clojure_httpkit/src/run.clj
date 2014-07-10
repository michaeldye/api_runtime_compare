(ns run
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [compojure.core :refer [defroutes GET context]]
            [org.httpkit.server :refer [run-server]]
            [taoensso.timbre :as timbre])
  (:gen-class))
(timbre/refer-timbre)

(defn fib [a b] (cons a (lazy-seq (fib b (+' b a)))))

(defn response [code body]
  {:status code
   :headers {"Content-Type" "text/plain"}
   :body (if (seq? body)
             (interpose " " body)
             body)
   })

(defn fast-routes [req]
  (let [uri (remove clojure.string/blank? (seq ^"[Ljava.lang.String;" (.split ^java.lang.String (.getPath ^java.net.URI (java.net.URI. (:uri req))) "/")))]
    (if (and (= (count uri) 3) (= "api" ^java.lang.String (first uri)))

      (let [n (+ 1 ^int (Integer/parseInt ^java.lang.String (nth uri 2)))]
        (case (second uri)
          "count" (response 200 (range 1 n) )
          "fib" (response 200 (take n (fib 0 1)) )
          "default" (response 404 "not found")))

      (response 404 "not found"))))

(defn -main [& args]
  (let [port 9009]
    (info "clojure_httpkit listening on port" port)
    (run-server fast-routes {:port port})))
