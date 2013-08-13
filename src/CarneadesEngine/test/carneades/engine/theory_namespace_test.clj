(ns carneades.engine.theory-namespace-test
  (:require [carneades.engine.theory-namespace :as tn]
            [carneades.engine.theory :as t]
            [clojure.test :refer :all]))

(deftest test-make-schemes-absolute-empty-namespaces
  (let [sexp1 '(p:a b c)
        theory {:sections
                [(t/make-section
                  :id :s1
                  :schemes
                  [(t/make-scheme :conclusion sexp1)])]}]
    (is (= (:conclusion (first (:schemes (first (-> theory :sections)))))
           sexp1))))

(deftest test-make-schemes-absolute-default-namespace
  (let [sexp1 '(p:a b c)
        sexp2 '(d e f)
        theory (t/make-theory :namespaces {"" "http://www.markosproject.eu/ontologies/copyright#"}
                              :sections
                              [(t/make-section
                                :id :s1
                                :schemes
                                [(t/make-scheme :conclusion sexp1)
                                 (t/make-scheme :conclusion sexp2)])])]
    (is (= (:conclusion (first (:schemes (first (-> theory :sections)))))
           sexp1))))
