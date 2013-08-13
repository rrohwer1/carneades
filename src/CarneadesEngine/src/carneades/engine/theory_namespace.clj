;;; Copyright (c) 2013 Fraunhofer Gesellschaft
;;; Licensed under the EUPL V.1.1

(ns carneades.engine.theory-namespace
  (:require
            [clojure.zip :as z]))

(defn change-schemes
  [section]
  (update-in section [:schemes] concat [{:myscheme 42}]))

(defn explore
  [theory]
  (let [zip (theory-zip theory)]
    (loop [loc zip]
      (if (z/end? loc)
        (z/root loc)
        (let [node (z/node loc)]
          (if (empty? (:schemes node))
            (recur (z/next loc))
            (do
              (prn "something")
              (prn (:schemes node))
              (recur (z/next (z/edit loc change-schemes))))))))))

(defn make-schemes-absolute
  [theory]
  (let [namespaces (:namespaces theory)
        sections (:sections theory)]
    (if (empty? namespaces)
      theory)))
