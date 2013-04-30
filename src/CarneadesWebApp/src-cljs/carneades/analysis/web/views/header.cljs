;;; Copyright (c) 2013 Fraunhofer Gesellschaft
;;; Licensed under the EUPL V.1.1

(ns carneades.analysis.web.views.header
  (:use [jayq.core :only [$ inner]]
        [jayq.util :only [log clj->js]]
        [carneades.analysis.web.i18n :only [i18n]])
  (:require [carneades.analysis.web.template :as tp]))

(defn get-title
  [title-or-key]
  (if (keyword? title-or-key)
    (i18n title-or-key)
    title-or-key))

(defn build-menu-item
  [item first last]
  (let [class (cond first "first"
                    last "last"
                    :else "")]
   (format "<li><a href=\"%s\" class=\"%s\">%s</a></li>"
           (:link item)
           class
           (i18n (:text item)))))

(defn build-menu
  [menu]
  (let [html (reduce (fn [html item]
                       (str html (build-menu-item item false false)))
           ""
           (rest (butlast menu)))]
    (str (build-menu-item (first menu) true false)
         html
         (build-menu-item (last menu) false true))))

(defn show-menu
  [menu]
  (inner ($ ".section-menu ul") (build-menu menu)))

(defn show
  ([title-or-key]
     (show title-or-key []))
  ([title-or-key menu]
     (let [title (get-title title-or-key)]
       (inner ($ ".topheader") (tp/get "header" {}))
       (inner ($ ".section-title") title)
       (show-menu menu))))

