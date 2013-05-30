;;; Copyright (c) 2013 Fraunhofer Gesellschaft
;;; Licensed under the EUPL V.1.1

(ns ^{:doc "Displays the projects on the admin page"}
  carneades.analysis.web.views.admin.project
  (:use [jayq.core :only [$ inner attr append]]
        [jayq.util :only [log]]
        [carneades.analysis.web.views.core :only [json]]
        [carneades.analysis.web.i18n :only [i18n]])
   (:require [carneades.analysis.web.views.header :as header]
            [carneades.analysis.web.template :as tp]
            [carneades.analysis.web.dispatch :as dispatch]))

(def state (atom {:selected nil}))

(defn export
  [project]
  (.open js/window (str js/IMPACT.wsurl "/export/" project ".zip")))

(dispatch/react-to #{:admin-export} (fn [_ msg]  (log "msg=" msg) (export (:project msg))))

(defn delete-successful
  []
  (.fetch js/PM.projects (clj->js {:async false})))

(defn delete
  [project]
  (when (and (js/confirm (i18n "delete_confirmation1"))
             (js/confirm (i18n "delete_confirmation2")))
    (.destroy (.get js/PM.projects project)
              (clj->js {:success show}))))

(dispatch/react-to #{:admin-delete} (fn [_ msg] (delete (:project msg))))

(defn on-project-checked
  "Changes the state of the selection when the user selects one or
  more projects "
  [event]
  (let [input ($ (.-target event))
        id (.-id (.-target event))
        checked (attr input "checked")]
    (if checked
      (swap! state assoc-in [:selected] id)
      (swap! state assoc-in [:selected] nil))))

(defn on-export-clicked
  [event]
  (.stopPropagation event)
  (when-let [project (:selected (deref state))]
    (dispatch/fire :admin-export {:project project}))
  false)

(defn on-delete-clicked
  [event]
  (.stopPropagation event)
  (when-let [project (:selected (deref state))]
    (log "project =" project)
    (dispatch/fire :admin-delete {:project project}))
  false)

(defn attach-listeners
  []
  (doseq [input ($ "input[type=radio]")]
    (.change ($ input) on-project-checked)))

(defn ^:export show
  []
  (header/show {:text :admin
                :link "#/admin/project"}
               [{:text :menu_import
                 :link "#/admin/import"}
                {:text :menu_export
                 :link "#/admin/export"
                 :on on-export-clicked}
                {:text :edit
                 :link "#/admin/edit/"}
                {:text :menu_delete
                 :link "#/admin/delete"
                 :on on-delete-clicked}])
  (inner ($ ".content")
         (tp/get "admin_project"
                 {:projects (json js/PM.projects)}))
  (attach-listeners))
