;;; Copyright (c) 2013 Fraunhofer Gesellschaft
;;; Licensed under the EUPL V.1.1

(ns carneades.web.license-analysis.views.debug.query
  (:use [jayq.core :only [$ inner attr append]]
        [carneades.analysis.web.views.core :only [json]])
  (:require [carneades.analysis.web.template :as tp]
            [carneades.analysis.web.views.header :as header]))

(defn ^:export show
  [project]
  (header/show {:text :home
                :link "#/home"})
  (inner ($ ".content") "Debug"))
