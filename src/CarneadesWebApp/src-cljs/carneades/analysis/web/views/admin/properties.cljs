;;; Copyright (c) 2013 Fraunhofer Gesellschaft
;;; Licensed under the EUPL V.1.1

(ns ^{:doc "Displays the properties of a project in the admin page"}
  carneades.analysis.web.views.admin.properties
  (:use [jayq.core :only [$ inner attr append]]
        [jayq.util :only [log]]
        [carneades.analysis.web.views.core :only [json]]
        [carneades.analysis.web.i18n :only [i18n]])
  (:require [carneades.analysis.web.views.header :as header]
            [carneades.analysis.web.template :as tp]
            [carneades.analysis.web.dispatch :as dispatch]))

(defn on-save-properties
  []
  (log "save properties"))

(defn on-cancel-properties
  []
  (log "cancel properties"))

(defn get-url
  [project]
  (str "admin/edit/" project "/properties"))

(defn set-url
  [project]
  (js/jQuery.address.value (get-url project)))

(defn ^:export show
  [project]
  (js/PM.load_project project)
  (header/show {:text :admin
                :link "#/admin/project"
                :on on-save-properties}
               [{:text :save
                 :link "#/admin/edit/properties/save"
                 :on on-save-properties}
                {:text :cancel
                 :link "#/admin/edit/properties/cancel"
                 :on on-cancel-properties}
                {:text :theories
                 :link "#/admin/edit/theories"}
                {:text :documents
                 :link "#/admin/edit/documents"}])
  (let [properties (js->clj (json js/PM.project) :keywordize-keys true)]
    (inner ($ ".content")
           (tp/get "admin_properties" {:title_input (:title properties)
                                       ;; TODO: multi-lingual
                                       :description_input (:en (:description properties))
                                       :schemes_input (:schemes properties)
                                       :policies_input (:policies properties)}))))
