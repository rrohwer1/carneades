;;; Copyright (c) 2013 Fraunhofer Gesellschaft
;;; Licensed under the EUPL V.1.1

(ns ^{:doc "Zipper for a theory. @See clojure.zip"}
  carneades.engine.theory.zip
  (:require [clojure.zip :as z]
            [carneades.engine.theory :as t]))

(defn- may-have-children?
  [node]
  (or (t/theory? node)
      (t/section? node)))

(defn- make-node
  [node children]
  (assoc node :sections children))

(defn theory-zip
  "Returns a zipper for a theory. The zipper navigates
through the sections of the theory."
  [theory]
  (z/zipper may-have-children?
            :sections
            make-node
            theory))
