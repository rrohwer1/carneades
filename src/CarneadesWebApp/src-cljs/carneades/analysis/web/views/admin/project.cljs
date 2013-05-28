(ns ^{:doc "Display the projects on the admin page"}
  carneades.analysis.web.views.admin.project
  (:use [jayq.core :only [$ inner attr append]]
        [jayq.util :only [log]]
        [carneades.analysis.web.views.core :only [json]])
   (:require [carneades.analysis.web.views.header :as header]
            [carneades.analysis.web.template :as tp]
            [carneades.analysis.web.dispatch :as dispatch]))

(def state (atom {:selected nil}))

(defn export
  [project]
  (.open js/window (str js/IMPACT.wsurl "/export/" project ".zip")))

(dispatch/react-to #{:admin-export}
                   (fn [_ msg]
                     (export (:project msg))))

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

(defn attach-listeners
  []
  (doseq [input ($ "input[type=radio]")]
    (.change ($ input) on-project-checked)))

(defn ^:export show
  []
  (header/show {:text :admin
                :link "#/home"}
               [{:text :menu_import
                 :link "#/admin/import"}
                {:text :menu_export
                 :link "#/admin/export"
                 :on on-export-clicked}
                {:text :edit
                 :link "#/admin/edit/"}
                {:text :menu_delete
                 :link "#/admin/delete"}])
  (inner ($ ".content")
         (tp/get "admin_project"
                 {:projects (json js/PM.projects)}))
  (attach-listeners))
