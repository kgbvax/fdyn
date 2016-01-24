;; fdyn, Copyright 2016 Ingomar Otter, based on:
;; Tinymasq, Copyright 2013 Ronen Narkis, narkisr.com
;; Licensed under the Apache License,
;; Version 2.0 (the "License") you may not use this file except in compliance with the License.
;; You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
;; Unless required by applicable law or agreed to in writing, software
;; distributed under the License is distributed on an "AS IS" BASIS,
;; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
;; See the License for the specific language governing permissions and
;; limitations under the License.
(ns tinymasq.api
  "Add/Remove hosts"
  (:require
    [tinymasq.config :refer (users)]
    [clojure.core.strint :refer (<<)]
    [clojure.pprint :refer (pprint)]
    [cheshire.core :as  cheshire]
    [taoensso.timbre :as timbre :refer (refer-timbre)]
    [tinymasq.store :refer (update-host del-host get-host list-hosts)]
    [compojure.core :refer (defroutes routes)]
    [ring.middleware.ssl :refer (wrap-ssl-redirect)]
    [ring.middleware.format :refer (wrap-restful-format)]
    [compojure.core :refer (GET POST PUT DELETE)]
    [compojure.route :as route]
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

(def data [
           {"title" "Gazpacho"
            "ingredients" "Ingredients: 2 onions, 2 garlic cloves peeled and minced, 1 cup of chopped green pepper, 2 cups water, 2 teaspoons salt, 1/3 teaspoon black pepper, 1/3 cup red wine vinegar, 1 cup peeled and chopped cucumber, 5 tablespoons olive oil"
            "directions" "Combine the onions, garlic, green peppers and tomatoes. Force through a sieve or puree in a blender. Add the salt, pepper and paprika. Add the olive oil gradually, beating steadily. Add the vinegar and water and stir well. Season to taste. Refrigerate and chill for at least two hours"}
           {"title" "Balsamic Mushrooms"
            "directions" "Place all ingredients in a (preferably nonstick) pan and let sit for a few minutes. Then cook covered over medium heat for about three minutes until they are soft. Remove the cover and cook until the liquid is almost gone, then serve."
            "ingredients" "12 mushrooms, 1/4 cup balsamic vinegar, 1/8 cup red wine"}
           ])

(defn recipes-helper []
  (cheshire/generate-string data))

(def index-page (slurp "resources/public/index.html"))

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
              :body   (cheshire/generate-string (list-hosts))})
           (GET "/" [] index-page)
           (GET "/recipes/" [] (recipes-helper))
           (route/resources "/"))

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
