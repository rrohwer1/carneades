(ns carneades.owl.translations
  (:import [org.semanticweb.owlapi.model AxiomType]))

(defn annotation-assertion-axiom?
  [axiom]
  (= (.getAxiomType axiom) AxiomType/ANNOTATION_ASSERTION))

(defn get-annotations-assertions
  [ontology]
  (filter annotation-assertion-axiom? (.getAxioms ontology)))

(defn text-annotation?
  [axiom]
  (prn axiom)
  (prn "subject" (.getSubject axiom))
  (prn "property" (.getProperty axiom))
  (prn "property.getIRI" (str (.getIRI (.getProperty axiom))))
  )

(defn get-text-annotations
  [ontology]
  (let [annotations-assertions (get-annotations-assertions ontology)
        text-annotations (filter text-annotation? annotations-assertions)
        ]
    text-annotations))
