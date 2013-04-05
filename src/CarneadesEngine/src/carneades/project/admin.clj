;;; Copyright (c) 2013 Fraunhofer Gesellschaft
;;; Licensed under the EUPL V.1.1

(ns ^{:doc "Management functions for the projects."}
  carneades.project.admin
  (:use [carneades.engine.utils :only [file-separator exists?]])
  (:require [carneades.config.config :as config]))

(defn- project?
  "Returns true if the director is a project."
  [dir]
  (and (.isDirectory dir)
       (exists? (str dir file-separator "properties.clj"))))

(defn list-projects
  "Returns a list of project names. A read on the disk is performed."
  []
  (let [projects-directory (config/properties :projects-directory)
        dirs (into [] (.listFiles (clojure.java.io/file
                                   projects-directory)))
        projects (filter project? dirs)]
    (map (memfn getName) projects)))
