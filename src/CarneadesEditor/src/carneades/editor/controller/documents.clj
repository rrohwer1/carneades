;;; Copyright © 2010 Fraunhofer Gesellschaft 
;;; Licensed under the EUPL V.1.1

(ns carneades.editor.controller.documents
  (:use clojure.contrib.def
        carneades.engine.argument
        carneades.editor.model.docmanager
        carneades.editor.view.viewprotocol))

(defvar *docmanager* (create-docmanager))
(defvar- *dirtyags* (atom #{}))

(defn set-ag-dirty [path id isdirty]
  (if isdirty
    (swap! *dirtyags* conj [path id])
    (swap! *dirtyags* disj [path id])))

(defn update-dirty-state [view path ag isdirty]
  (set-ag-dirty path (:id ag) isdirty)
  (set-dirty view path ag isdirty))

(defn update-undo-redo-statuses [view path id]
  (prn "update-undo-redo-statuses")
  (set-can-undo view path id (can-undo-section? *docmanager* [path :ags id]))
  (set-can-redo view path id (can-redo-section? *docmanager* [path :ags id])))

(defn is-ag-dirty [path id]
  (contains? (deref *dirtyags*) [path id]))

(defn get-ag [lkifpath id]
  (get-section-content *docmanager* [lkifpath :ags id]))

(defn get-ags-id [lkifpath]
  (let [agsid (get-all-sectionskeys *docmanager* [lkifpath :ags])]
    agsid))

(defn init-counters [path]
  (add-section *docmanager* [path :graph-counter] 1)
  (doseq [id (get-ags-id path)]
    (add-section *docmanager* [path id :stmt-counter] 1)))

(defn init-stmt-counter [path id]
  (add-section *docmanager* [path id :stmt-counter] 1))

(defn stmt-counter-value [path id]
  (let [v (get-section-content *docmanager* [path id :stmt-counter])]
    (update-section *docmanager* [path id :stmt-counter] (inc v))
    v))

(defn graph-counter-value [path]
  (let [v (get-section-content *docmanager* [path :graph-counter])]
    (update-section *docmanager* [path :graph-counter] (inc v))
    v))

(defn gen-statement-content [path ag]
  (let [stmt (str "statement_" (stmt-counter-value path (:id ag)))]
    (if (statement-node ag stmt)
      (gen-statement-content path ag)
      stmt)))

(defn gen-graph-title [path]
  (let [title (str "graph" (graph-counter-value path))
        titles (set (map :title (map #(get-ag path %) (get-ags-id path))))]
    (if (get titles title)
      (gen-graph-title path)
      title)))