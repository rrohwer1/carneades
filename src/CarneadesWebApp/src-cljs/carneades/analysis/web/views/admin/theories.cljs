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

(def state (atom {:selected nil}))

(defn download
  [theories]
  (.open js/window
         (str js/IMPACT.wsurl
              "/project/"
              js/PM.project.id
              "/theories/"
              theories
              ".clj")))

(dispatch/react-to #{:admin-theories-download} (fn [_ msg] (download (:theories msg))))

(defn on-theory-checked
  "Changes the state of the selection when the user selects one or
  more projects "
  [event]
  (let [input ($ (.-target event))
        id (.-id (.-target event))
        checked (attr input "checked")]
    (if checked
      (swap! state assoc-in [:selected] id)
      (swap! state assoc-in [:selected] nil))))

(defn on-download-clicked
  [event]
  (.stopPropagation event)
  (when-let [name (:selected (deref state))]
    (dispatch/fire :admin-theories-download {:theories name}))
  false)

(defn attach-listeners
  []
  (doseq [input ($ "input[type=radio]")]
    (.change ($ input) on-theory-checked)))

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
                 :link "#/admin/edit/theories/upload"}
                {:text :download
                 :link "#/admin/edit/theories/download"
                 :on on-download-clicked
                 }
                {:text :edit
                 :link "#/admin/edit/theories/edit"}
                {:text :menu_delete
                 :link "#/admin/edit/theories/delete"}])
  (let [theories (.-theories (json (aget js/PM.projects_theories project)))]
    (inner ($ ".content")
           (tp/get "admin_theories" {:theories theories}))
    (attach-listeners)))
