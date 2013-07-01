;;; Copyright (c) 2013 Fraunhofer Gesellschaft
;;; Licensed under the EUPL V.1.1

(ns ^{:doc ""}
  carneades.web.license-analysis.model.analysis
  (:use [clojure.tools.logging :only (info debug error)])
  (:require [carneades.engine.shell :as shell]
            [carneades.engine.scheme :as theory]
            [carneades.engine.argument-graph :as ag]
            [carneades.engine.ask :as ask]
            [carneades.engine.dialog :as dialog]
            [carneades.project.admin :as project]
            [carneades.policy-analysis.web.logic.askengine :as policy]
            [carneades.policy-analysis.web.logic.questions :as questions]))

(defn- start-engine
  [project theories entity]
  (let [query '(?x a b) ;; TODO, instanciate the query from the theories
        loaded-theories (project/load-theory project theories)
        [argument-from-user-generator questions send-answer]
        (ask/make-argument-from-user-generator (fn [p] (questions/askable? loaded-theories p)))
        ag (ag/make-argument-graph)
        engine (shell/make-engine ag 500 #{}
                                  (list (theory/generate-arguments-from-theory loaded-theories)
                                        argument-from-user-generator))
        future-ag (future (shell/argue engine query))
        analysis {:future-ag future-ag
                  :questions questions
                  :send-answer send-answer
                  :dialog (dialog/make-dialog)
                  :last-id 0}
        analysis (policy/get-ag-or-next-question analysis)]
    (select-keys analysis [:dialog :last-id])))

(defn analyse
  "Begins an analysis of a given software entity. The theories inside project is used.
Returns a set of questions for the frontend."
  [project theories entity]
  (start-engine project theories entity))

;; http://localhost:8080/carneadesws/license-analysis/analyse?entity=http://markosproject.eu/android&project=copyright&theories=copyright_policies
