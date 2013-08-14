;;; Copyright (c) 2013 Fraunhofer Gesellschaft
;;; Licensed under the EUPL V.1.1

(ns ^{:doc "Namespaces for theories."}
  carneades.engine.theory.namespace
  (:require [clojure.walk :as w]
            [clojure.zip :as z]
            [carneades.engine.theory.zip :as tz]
            [carneades.engine.statement :as st]
            [carneades.engine.argument :as a])
  (:refer-clojure :exclude [name namespace]))

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
  (cond (st/variable? atom) atom
        (symbol? atom) (let [ns (namespace atom)
                             n (name atom)
                             iri (namespaces ns)]
                         (when (and (nil? iri) (not= ns ""))
                           (throw (ex-info (str "Missing namespace '" ns "'") {})))
                         (symbol (str iri n)))
        :else
        (map #(to-absolute-atom % namespaces) atom)))

(defn to-absolute-statement
  "Converts this statement to an absolute statement."
  [statement namespaces]
  (update-in statement [:atom] to-absolute-atom namespaces))

(defn to-absolute-literal
  "Recursively convert each of the atom of the literal to an absolute atom."
  [literal namespaces]
  (cond (empty? namespaces) literal
        (st/sliteral? literal) (to-absolute-atom literal namespaces)
        :else (to-absolute-statement literal namespaces)))

(defn to-absolute-premise
  "Converts a premise to an absolute premise"
  [premise namespaces]
  (if (st/literal? premise)
    (to-absolute-literal premise namespaces)
    (update-in premise [:statement] to-absolute-literal namespaces)))

(defn to-absolute-premises
  "Converts a seq of premises to absolute premises"
  [premises namespaces]
  (into [] (map #(to-absolute-premise % namespaces) premises)))

(defn to-absolute-scheme
  "Converts the scheme to an absolute scheme."
  [scheme namespaces]
  (-> scheme
      (update-in [:conclusion] to-absolute-literal namespaces)
      (update-in [:premises] to-absolute-premises namespaces)
      (update-in [:exceptions] to-absolute-premises namespaces)
      (update-in [:assumptions] to-absolute-premises namespaces)))

(defn to-absolute-schemes
  "Converts the schemes to absolute schemes."
  [schemes namespaces]
  (into [] (map #(to-absolute-scheme % namespaces) schemes)))

(defn to-absolute-section
  "Transforms the atoms contained in the schemes of a section to absolute atoms."
  [section namespaces]
  (update-in section [:schemes] to-absolute-schemes namespaces))

(defn to-absolute-theory
  "Converts the atoms contained in the schemes of the theory to
  absolute atoms."
  [theory namespaces]
  (loop [loc (tz/theory-zip theory)]
    (if (z/end? loc)
      (z/root loc)
      (let [loc (z/edit loc to-absolute-section namespaces)]
        (recur (z/next loc))))))
