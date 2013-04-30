(ns carneades.analysis.web.views.header
  (:use [jayq.core :only [$ inner]])
  (:require [carneades.analysis.web.template :as tp]))

(defn show
  []
  (inner ($ ".topheader") (tp/get "header" {})))

