;;; Copyright (c) 2013 Fraunhofer Gesellschaft
;;; Licensed under the EUPL V.1.1

(ns carneades.web.license-analysis.views.introduction
  (:use [jayq.core :only [$ inner attr append]]
        [carneades.analysis.web.views.core :only [json]])
  (:require [carneades.analysis.web.template :as tp]
            [carneades.analysis.web.views.header :as header]
            [carneades.analysis.web.dispatch :as dispatch]))

(defn on-reload-projects-successfull
  []
  (.fetch js/PM.projects (clj->js {:async false}))
  (js/PM.notify "Reload successful"))

(defn reload-projects
  [msg]
  (js/PM.ajax_get (str js/IMPACT.wsurl "/debug/reload-projects")
                  on-reload-projects-successfull
                  js/PM.on_error))

(dispatch/react-to #{:license-analysis-reload-projects}
                   (fn [_ msg] (reload-projects msg)))

(defn on-reload-projects
  []
  (dispatch/fire :license-analysis-reload-projects {}))

(defn attach-listeners
  []
  (.click ($ ".reload-projects") on-reload-projects))

(defn ^:export show
  []
  (header/show {:text :home
                :link "#/home"})
  (inner ($ ".content") (tp/get "license_introduction" {}))
  (attach-listeners))
