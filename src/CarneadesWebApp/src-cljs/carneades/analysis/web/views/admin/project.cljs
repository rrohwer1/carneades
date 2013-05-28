(ns carneades.analysis.web.views.admin.project
  (:use [jayq.core :only [$ inner attr append]]
        [jayq.util :only [log]]
        [carneades.analysis.web.views.core :only [json]])
   (:require [carneades.analysis.web.views.header :as header]
            [carneades.analysis.web.template :as tp]
            [carneades.analysis.web.dispatch :as dispatch]))

(defn on-export-clicked
  []
  (log "export clicked")
  (let [project "copyright"]
    (dispatch/fire :admin-export {:project project})))

(defn export
  [project]
  (log "exporting project")
  (log project))

(dispatch/react-to #{:admin-export} (fn [_ msg] (export (:project msg))))

(defn attach-listeners
  []
  )

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
