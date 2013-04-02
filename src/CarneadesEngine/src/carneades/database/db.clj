;;; Copyright (c) 2011-2013 Fraunhofer Gesellschaft
;;; Licensed under the EUPL V.1.1
;;;

(ns ^{:doc "Utilities for interacting with databases."}
  carneades.database.db
  (require [carneades.config.reader :as config]
           [clojure.java.jdbc :as jdbc])
  (:import java.io.File))

(defmacro with-db [db & body]   
  `(jdbc/with-connection 
     ~db
     (jdbc/transaction ~@body)))

(defmacro test-db 
  "For testing and development.  Doesn't do any 
   any error handling so don't use in production code."
  [db & body]   
  `(jdbc/with-connection 
           ~db
           (jdbc/transaction ~@body)))

;;; Databases

(def default-db-protocol (config/properties "database-protocol"
                                            "file"))

;; (def default-db-host (config/properties "database-host"
;;                                         (str (System/getProperty "user.dir")
;;                                              File/separator
;;                                              "data/databases")))

(def projects-directory (config/properties "project-directories"
                                             (str (System/getProperty "user.dir")
                                                  File/separator
                                                  "projects")))

(defn dbfilename
  "Returns the filename of a database."
  [project dbname]
  (str projects-directory "/" project "/databases/" dbname ".h2.db"))

;; (defn fetch-databases-names
;;   "Looks on the disk to find all existing databases. Returns their names"
;;   []
;;   (keep #(second (re-find #"(.*)\.h2\.db$" %))
;;         (map (memfn getName)
;;              (file-seq (clojure.java.io/file default-db-host)))))

(defn make-connection
  "Returns a map describing a database connection."
  ([project-name db-name username passwd & options]
     (let [options (apply hash-map options)
           db-protocol (:protocol options default-db-protocol) ;; "file|mem|tcp"
           db-directory (str projects-directory File/separator
                             project-name File/separator "databases") ;; "path|host:port"
           db-host (str db-protocol "://" db-directory "/" db-name)
           ]
       (prn "db-host " db-host)
       {:classname   "org.h2.Driver" 
        :subprotocol "h2"
        :subname db-host
        :user  username
        :password passwd})))