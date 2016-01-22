(comment
  Tinymasq, Copyright 2013 Ronen Narkis, narkisr.com
  Licensed under the Apache License,
  Version 2.0 (the "License") you may not use this file except in compliance with the License.
  You may obtain a copy of the License at http: / / www.apache.org/licenses / LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License. )

(ns tinymasq.api
  "Add/Remove hosts"
  (:require
    [tinymasq.config :refer (users)]
    [clojure.core.strint :refer (<<)]
    [clojure.pprint :refer (pprint)]
    [taoensso.timbre :as timbre :refer (refer-timbre)]
    [tinymasq.store :refer (update-host del-host get-host)]
    [compojure.core :refer (defroutes routes)]
    [ring.middleware.ssl :refer (wrap-ssl-redirect)]
    [ring.middleware.format :refer (wrap-restful-format)]
    [compojure.core :refer (GET POST PUT DELETE)]
    [cemerick.friend :as friend]
    (cemerick.friend [workflows :as workflows]
                     [credentials :as creds])))

(refer-timbre)

(defn error-wrap
  "A catch all error handler"
  [app]
  (fn [req]
    (try
      (app req)
      (catch Throwable e
        (error e)
        {:body (<< "Unexpected error ~(.getMessage e) of type ~(class e) contact admin for more info") :status 500}))))


(defroutes hosts
           (GET "/nic/update" {{host :host myip :myip description :description}
                               :params}
             (trace "update" host myip description)
             (friend/authenticated
               ;  (update-host auth host myip description)
               {:status 200
                :body   "i hear you"}))
           (GET "/services" {}
             (trace "services")
             {:status 200
              :body   "perhaps"}))

(def user #{::user})
(def admin #{::admin})

(derive ::admin ::user)

(defn sign-in-resp
  [req]
  {:status 401 :body "not valid creds"})

(defn- fdyn-credential-fn [workflow]
  (trace "fdyn crds")
  (creds/bcrypt-credential-fn)
  (pprint workflow)
  true)



(defn secured-app [routes]
  (friend/authenticate routes
                       {:allow-anon?             true
                        :credential-fn           fdyn-credential-fn
                        :unauthenticated-handler sign-in-resp
                        :workflows               [(workflows/http-basic :realm "basic-fdyn")]}))

(defn app []
  (-> (routes hosts)
      (secured-app)
      (wrap-restful-format :formats [:json-kw :edn :yaml-kw :yaml-in-html])
      (error-wrap)))
