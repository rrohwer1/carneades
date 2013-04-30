;;; Copyright (c) 2013 Fraunhofer Gesellschaft
;;; Licensed under the EUPL V.1.1

(ns carneades.analysis.web.views.project
  (:use [jayq.core :only [$ inner]]
        [jayq.util :only [log]]
        [carneades.analysis.web.views.core :only [json]])
  (:require [carneades.analysis.web.template :as tp]
            [carneades.analysis.web.views.header :as header]))

(defn ^:export show
  [project]
  (let [proj (.get js/PM.projects project)
        pdata (json proj)]
    (set! PM.project proj)
    (header/show (.-title pdata) [{:text :arguments
                                   :link (format "#/arguments/outline/%s/%s" project "main")}
                                  {:text :guidedtour
                                   :link (format "#/tour/%s" project)}
                                  {:text :policies
                                   :link (format "#/policies/%s" project)}])
    (inner ($ ".content") (tp/get "project"
                                  {:introduction (js/PM.markdown_to_html (.-introduction pdata))}))))
