;;; Copyright (c) 2013 Fraunhofer Gesellschaft
;;; Licensed under the EUPL V.1.1

(ns carneades.analysis.web.views.header
  (:use [jayq.core :only [$ inner]]
        [jayq.util :only [log clj->js]])
  (:require [carneades.analysis.web.template :as tp]))

(defn get-title
  [title-or-key]
  (if (keyword? title-or-key)
    (js/jQuery.i18n.prop (name title-or-key))
    title-or-key))

(defn show
  [title-or-key]
  (let [title (get-title title-or-key)]
    (inner ($ ".topheader") (tp/get "header" {}))
    (inner ($ ".section-title") title)))

