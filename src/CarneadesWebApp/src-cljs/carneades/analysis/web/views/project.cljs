(ns carneades.analysis.web.views.project
  (:use [jayq.core :only [$ inner]]
        [jayq.util :only [log]]
        [carneades.analysis.web.views.core :only [json]])
  (:require [carneades.analysis.web.template :as tp]
            [carneades.analysis.web.views.header :as header]))

(defn ^:export show
  [project]
  (log "project=" project)
  (let [pdata (json (.get js/PM.projects project))]
    (header/show (.-title pdata))
    (inner ($ ".content") "project page";; (tp/get "project" {})
           )))
