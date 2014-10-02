(ns run
  (:require [ring.util.response :refer [response]]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [compojure.core :refer [defroutes GET context]]
            [org.httpkit.server :refer [run-server]]
            [taoensso.timbre :as timbre])
  (:gen-class))
(timbre/refer-timbre)

; from Clojure Programming examples, adapted to BigInt with +'
(def fib-seq (lazy-cat [0 1] (map +' (rest fib-seq) fib-seq)))
(defn plain [s] (interpose " " s))

(defn fast-response [code body]
  {:status code
   :headers {"Content-Type" "text/plain"}
   :body (if (seq? body)
             (plain body)
             body)
   })

(defroutes routes
  ; context for entire app
  (context "/api" []
    (GET "/count/:n" [n]
         (response
           (plain (range 1 (+ 1 (Integer/parseInt n))))))
    (GET "/fib/:n" [n]
         (response
           (plain (take (+ 1 (Integer/parseInt n)) fib-seq)))))
  (route/not-found "not found"))

(defn fast-routes [req]
  (let [uri (remove clojure.string/blank? (seq ^"[Ljava.lang.String;" (.split ^java.lang.String (.getPath ^java.net.URI (java.net.URI. (:uri req))) "/")))]
    (if (and (= (count uri) 3) (= "api" ^java.lang.String (first uri)))

      (let [n (+ 1 ^int (Integer/parseInt ^java.lang.String (nth uri 2)))]
        (case (second uri)
          "count" (fast-response 200 (range 1 n) )
          "fib" (fast-response 200 (take n fib-seq) )
          "default" (fast-response 404 "not found")))

      (fast-response 404 "not found"))))

(defn -main [& args]
  (let [port 9009]
    (info "clojure_httpkit listening on port" port)
    (run-server routes {:port port})))
