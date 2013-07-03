;;; Copyright (c) 2013 Fraunhofer Gesellschaft
;;; Licensed under the EUPL V.1.1

(ns ^{:doc ""}
  carneades.web.license-analysis.model.analysis
  (:use [clojure.tools.logging :only (info debug error)]
        [carneades.engine.utils :only [safe-read-string]])
  (:require [clojure.pprint :as pp]
            [carneades.engine.shell :as shell]
            [carneades.engine.scheme :as theory]
            [carneades.engine.argument-graph :as ag]
            [carneades.engine.ask :as ask]
            [carneades.engine.dialog :as dialog]
            [carneades.project.admin :as project]
            [carneades.policy-analysis.web.logic.askengine :as policy]
            [carneades.policy-analysis.web.logic.questions :as questions]
            [carneades.engine.triplestore :as triplestore]
            [edu.ucdenver.ccp.kr.sparql :as sparql]))

(def markos-triplestore-endpoint "http://markos.man.poznan.pl/openrdf-sesame")
(def markos-repo-name "markos_test_sp2")
(def markos-namespaces [["top" "http://www.markosproject.eu/ontologies/top#"]
                        ["reif" "http://www.markosproject.eu/ontologies/reification#"]
                        ["soft" "http://www.markosproject.eu/ontologies/software#"]
                        ["lic" "http://www.markosproject.eu/ontologies/licenses#"]
                        ["kb" "http://markosproject.eu/kb/"]])

(defn- start-engine
  [project theories entity]
  (let [query '(may-publish the-person the-work) ;; TODO, instanciate the query from the theories
        loaded-theories (project/load-theory project theories)
        [argument-from-user-generator questions send-answer]
        (ask/make-argument-from-user-generator (fn [p] (questions/askable? loaded-theories p)))
        ag (ag/make-argument-graph)
        engine (shell/make-engine ag 500 #{}
                                  (list (theory/generate-arguments-from-theory loaded-theories)
                                        argument-from-user-generator))
        future-ag (future (shell/argue engine query))
        analysis {:ag nil
                  :lang :en
                  :query query
                  :policies loaded-theories
                  :future-ag future-ag
                  :questions questions
                  :send-answer send-answer
                  :dialog (dialog/make-dialog)
                  :last-id 0}
        analysis (policy/get-ag-or-next-question analysis)]
    (select-keys analysis [:dialog :last-id])))

;; http://localhost:8080/carneadesws/license-analysis/analyse?entity=http://markosproject.eu/android&project=copyright&theories=copyright_policies

(defn analyse
  "Begins an analysis of a given software entity. The theories inside project is used.
Returns a set of questions for the frontend."
  [project theories entity]
  (prn "project=" project)
  (prn "theories=" theories)
  (prn "entity=" entity)
  (start-engine project theories entity))

(defn debug-query
  "Returns the result of query in the triplestore"
  [endpoint repo-name query limit]
  (let [conn (triplestore/make-conn endpoint
                                    repo-name
                                    markos-namespaces)]
    (binding [sparql/*select-limit* limit]
      {:result (pp/write (sparql/query (:kb conn) (safe-read-string query))
                         :stream nil)})))
