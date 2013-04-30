;;; Copyright (c) 2013 Fraunhofer Gesellschaft
;;; Licensed under the EUPL V.1.1

(ns carneades.analysis.web.views.header
  (:use [jayq.core :only [$ inner]]
        [jayq.util :only [log clj->js]])
  (:require [carneades.analysis.web.template :as tp]))

(defn show
  [title-key]
  (let [title (js/jQuery.i18n.prop (name title-key))]
    (inner ($ ".topheader") (tp/get "header" {}))
    (inner ($ ".section-title") title)))

