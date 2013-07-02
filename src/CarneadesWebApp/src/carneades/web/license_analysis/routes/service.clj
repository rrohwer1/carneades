;;; Copyright (c) 2013 Fraunhofer Gesellschaft
;;; Licensed under the EUPL V.1.1

(ns ^{:doc "HTTP routes definitions for the license-analysis REST service."}
  carneades.web.license-analysis.routes.service
  (:use [compojure.core :only [defroutes GET POST]])
  (:require [carneades.web.license-analysis.model.analysis :as analysis]))

(defroutes license-analysis-routes
  (GET "/analyse" {{project :project
                    theories :theories
                    entity :entity} :params}
       {:body (analysis/analyse project theories entity)})

  ;; ex input: {"query": "(?/x ?/y ?/z)", "limit": 42}
  (POST "/debug/query" {{query :query
                         limit :limit
                         endpoint :endpoint
                         repo-name :repo-name} :params}
        {:body (analysis/debug-query endpoint repo-name query limit)}))
