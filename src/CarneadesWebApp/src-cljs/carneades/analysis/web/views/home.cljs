(ns carneades.analysis.web.views.home
  (:use [jayq.core :only [$ inner attr append]])
  (:require [carneades.analysis.web.template :as tp]
            [carneades.analysis.web.views.header :as header]))

(defn ^:export show
  []
  (header/show :home)
  (inner ($ ".content") (tp/get "home" {:projects [{:name "Default" :created "May 2012"}
                                                   {:name "Copyright in the Knowledge Economy"
                                                    :created "June 2013"}]}))

  )
