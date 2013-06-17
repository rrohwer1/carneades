;;; Copyright (c) 2013 Fraunhofer Gesellschaft
;;; Licensed under the EUPL V.1.1

(ns carneades.analysis.web.views.description-editor
  (:use [jayq.core :only [$ inner attr append]]
        [jayq.util :only [log]])
  (:refer-clojure :exclude [get]))

(defn get
  [])

(defn- on-tab-selected
  [event ui]
  (log "tab-selected: ")
  (log ui)
  (let [tab (.-tab ui)
        href (.attr ($ tab) "href")
        lang (subs href (- (count href) 2))]
    (log "lang=" )
    (log lang))
  false)

(defn show
  [selector]
  (.tabs ($ selector) (clj->js {:select on-tab-selected
                               :selected 0}))
  (.markItUp ($ (str selector " .description")) js/mySettings))
