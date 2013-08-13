;;; Copyright (c) 2013 Fraunhofer Gesellschaft
;;; Licensed under the EUPL V.1.1

(ns carneades.engine.theory-namespace
  (:require [carneades.engine.theory :as t]
            [clojure.zip :as z]))

(defn may-have-children?
  [node]
  (or (t/theory? node)
      (t/section? node)))

(defn get-children
  [node]
  (:sections node))

(defn make-node
  [node children]
  (prn "node =" node)
  (prn "children = " children)
  (assoc node :sections children))

(defn theory-zip
  [root]
  (z/zipper may-have-children?
            get-children
            make-node
            root))

(defn make-schemes-absolute
  [theory]
  (let [namespaces (:namespaces theory)
        sections (:sections theory)]
    (if (empty? namespaces)
      theory)))
