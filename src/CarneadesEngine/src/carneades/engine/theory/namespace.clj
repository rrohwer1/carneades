;;; Copyright (c) 2013 Fraunhofer Gesellschaft
;;; Licensed under the EUPL V.1.1

(ns ^{:doc "Namespaces for theories."}
    carneades.engine.theory.namespace
  (:require [clojure.walk :as w])
  (:refer-clojure :exclude [name]))

(defn namespace
  "Returns a string describing the namespace of atom."
  [atom]
  (let [n (clojure.core/name atom)
        match (re-seq #"(.+):.+" n)]
    (if match
      (second (first match))
      "")))

(defn name
  "Returns a string describing the name of atom."
  [atom]
  (let [n (clojure.core/name atom)
        match (re-seq #".+:(.+)" n)]
    (if match
      (second (first match))
      atom)))

(defn to-absolute-atom
  "Converts this atom to an absolute atom. Throws an exception if a
  namespace is missing but necessary for the transformation."
  [atom namespaces]
  (if (symbol? atom)
    (let [ns (namespace atom)
          n (name atom)
          iri (namespaces ns)]
      (when (and (nil? iri) (not= ns ""))
        (throw (ex-info (str "Missing namespace '" ns "'") {})))
      (symbol (str iri n)))
    atom))

(defn to-absolute-literal
  "Recursively convert each of the atom of the literal to an absolute atom."
  [literal namespaces]
  (if (empty? namespaces)
    literal
    (w/postwalk #(to-absolute-atom % namespaces) literal)))
