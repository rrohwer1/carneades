(ns carneades.web.info
  (:use [carneades.web.pack :only [argument-data pack-argument]])
  (:require [carneades.database.argument-graph :as ag-db]))

(defn arg-info
  "Returns information concerning the argument and its context."
  [id]
  (let [arg (ag-db/read-argument (str id))
        arg (pack-argument arg)
        undercutters-data (doall (map argument-data (:undercutters arg)))
        rebuttals-data (doall (map argument-data (:rebuttals arg)))
        dependents-data (doall (map argument-data (:dependents arg)))]
    (assoc arg
      :undercutters-data undercutters-data
      :rebuttals-data rebuttals-data
      :dependents-data dependents-data)))
