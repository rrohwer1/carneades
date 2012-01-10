;;; Copyright © 2010-2011 Fraunhofer Gesellschaft
;;; Licensed under the EUPL V.1.1

(ns carneades.maps.lacij
  (:use carneades.engine.statement
        carneades.maps.lacij-export))

;; We don't use a Protocol here since
;; they don't deal correctly with optional arguments
;; and overloading of functions definitions
;;

(defn export
  [ag filename & options]
  (export-ag ag literal->str filename (apply hash-map options)))

(defn export-str
  [ag & options]
  (export-ag-str ag literal->str (apply hash-map options)))

(defn view
  [ag filename & options]
  (throw (Exception. "NYI")))
