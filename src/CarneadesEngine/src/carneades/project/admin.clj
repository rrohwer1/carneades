;;; Copyright (c) 2013 Fraunhofer Gesellschaft
;;; Licensed under the EUPL V.1.1

(ns ^{:doc "Management functions for the projects."}
  carneades.project.admin
  (:use [carneades.engine.utils :only [file-separator exists?]])
  (:require [carneades.config.config :as config]
            [carneades.engine.scheme :as theory]))

(def projects-directory (config/properties :projects-directory))

(defn- project?
  "Returns true if the director is a project."
  [dir]
  (and (.isDirectory dir)
       (exists? (str dir file-separator "properties.clj"))))

(defn list-projects
  "Returns a list of project names. A read on the disk is performed."
  []
  (let [dirs (into [] (.listFiles (clojure.java.io/file projects-directory)))
        projects (filter project? dirs)]
    (map (memfn getName) projects)))

(defn load-project-properties
  "Returns the project properties as a map."
  [project]
  (let [project-path (str projects-directory file-separator project)
        properties-path (str project-path file-separator "properties.clj")
        project-properties (config/read-properties properties-path)]
    project-properties))

(defn load-policy
  "Loads the policy of a project"
  [project project-properties]
  (when-let [policy-properties (:policy project-properties)]
    (let [project-path (str projects-directory file-separator project)
          {:keys [namespace variable path]} policy-properties
          policy-path (str project-path file-separator path ".clj")
          policy (theory/load-theory policy-path namespace variable)]
      policy)))

(defn load-project
  "Loads the configuration of a project and its policy. Returns a map
representing the project."
  [project]
  (let [project-properties (load-project-properties project)
        policy-properties (:policy project-properties)
        policy (load-policy project project-properties)]
    {:policy policy
     :properties project-properties}))
