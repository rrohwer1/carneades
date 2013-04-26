(ns carneades.analysis.web.views.home
  (:use [jayq.core :only [$ inner attr append]])
  (:require [carneades.analysis.web.template :as tp]))

(defn ^:export show
  []
  (inner ($ "#pm") (tp/get "header" {})))
