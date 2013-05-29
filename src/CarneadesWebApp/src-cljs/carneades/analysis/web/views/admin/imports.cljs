;;; Copyright (c) 2013 Fraunhofer Gesellschaft
;;; Licensed under the EUPL V.1.1

(ns ^{:doc "Displays the projects import page"}
  ;; there is a bug if naming this ns 'import',
  ;; ClojureScript generates a import$ object
  carneades.analysis.web.views.admin.imports
  (:use [jayq.core :only [$ inner attr append]]
        [jayq.util :only [log]]
        [carneades.analysis.web.views.core :only [json]]
        [carneades.analysis.web.i18n :only [i18n]])
  (:require [carneades.analysis.web.views.header :as header]
            [carneades.analysis.web.template :as tp]
            [carneades.analysis.web.dispatch :as dispatch]))

(defn on-upload-complete
  []
  (log "upload finished"))

(defn attach-listeners
  []
  (js/Dropzone. "div#dropzone" (clj->js {:url (str js/IMPACT.wsurl "/import")
                                         :dictDefaultMessage (i18n "drop_or_click")
                                         :complete on-upload-complete })))

(defn ^:export show
  []
  (header/show {:text :admin
                :link "#/admin/project"}
               [])
  (inner ($ ".content")
         (tp/get "admin_import" {}))
  (attach-listeners))
