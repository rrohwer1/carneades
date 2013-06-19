;;; Copyright (c) 2013 Fraunhofer Gesellschaft
;;; Licensed under the EUPL V.1.1

(ns ^{:doc "Displays the theories of a project in the admin page"}
  carneades.analysis.web.views.admin.theories
  (:use [jayq.core :only [$ inner attr append]]
        [jayq.util :only [log]]
        [carneades.analysis.web.views.core :only [json]]
        [carneades.analysis.web.i18n :only [i18n]])
  (:require [carneades.analysis.web.views.header :as header]
            [carneades.analysis.web.template :as tp]
            [carneades.analysis.web.dispatch :as dispatch]
            [carneades.analysis.web.views.description-editor :as description]
            ))

(defn get-url
  [project]
  (str "admin/edit/" project "/theories"))

(defn set-url
  [project]
  (js/jQuery.address.value (get-url project)))

(defn ^:export show
  [project]
  (js/PM.load_project project)
  (header/show {:text :admin
                :link (str "#/admin/" project)}
               [{:text :upload
                 :link "#/admin/edit/theories/upload"}{:text :download
                 :link "#/admin/edit/theories/download"}
                {:text :edit
                 :link "#/admin/edit/theories/edit"}
                {:text :menu_delete
                 :link "#/admin/edit/theories/delete"}])
  (let [theories []]
    (inner ($ ".content")
           (tp/get "admin_theories" {:theories theories}))))
