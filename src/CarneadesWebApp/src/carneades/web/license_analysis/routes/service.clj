;;; Copyright (c) 2013 Fraunhofer Gesellschaft
;;; Licensed under the EUPL V.1.1

(ns ^{:doc "HTTP routes definitions for the license-analysis REST service."}
  carneades.web.license-analysis.routes.service
  (:use [clojure.pprint :as pprint]
        [compojure.core :only [defroutes GET POST]]
        [carneades.engine.utils :only [safe-read-string]])
  (:require [carneades.web.license-analysis.model.analysis :as analysis]))

(defroutes license-analysis-routes
  (POST "/analyse" {{project :project
                     theories :theories
                     entity :entity
                     query :query} :params}
        (let [query (safe-read-string query)]
          {:body (analysis/analyse project theories entity query)}))

  (POST "/send-answers" {{answers :answers
                          uuid :uuid} :params}
        {:body (analysis/process-answers answers uuid)})

  (POST "/debug/query" {{query :query
                         limit :limit
                         endpoint :endpoint
                         repo-name :repo-name} :params}
        {:body (analysis/debug-query endpoint repo-name query limit)}))
