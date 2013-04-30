(ns carneades.analysis.web.views.home
  (:use [jayq.core :only [$ inner attr append]])
  (:require [carneades.analysis.web.template :as tp]
            [carneades.analysis.web.views.header :as header]))

(defn ^:export show
  []
  (header/show)
  (inner ($ ".content") (tp/get "home" {}))
  )
