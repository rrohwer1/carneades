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

(def default-db-protocol (config/properties "database-protocol" "file"))
(def default-db-host (config/properties "database-host"
                                        (str (System/getProperty "user.dir")
                                             File/separator
                                             "data/databases")))

(defn dbfilename
  "Returns the filename of a database."
  [dbname]
  (str default-db-host "/" dbname ".h2.db"))

(defn fetch-databases-names
  "Looks on the disk to find all existing databases. Returns their names"
  []
  (keep #(second (re-find #"(.*)\.h2\.db$" %))
        (map (memfn getName)
             (file-seq (clojure.java.io/file default-db-host)))))

(defn make-database-connection  
  "Returns a map describing a database connection.
   Available options are :host and :protocol.
   If not specified they default to theirs values
   in ~/.carneades.properties and if not specified there
   to '$PWD/data/databases' and 'file' respectively."
  [db-name username passwd & options]
  (let [options (apply hash-map options)
        db-protocol (:protocol options default-db-protocol) ;; "file|mem|tcp"
        db-host (:host options default-db-host) ;; "path|host:port" 
        ]
    {:classname   "org.h2.Driver" 
     :subprotocol "h2"
     :subname (str db-protocol "://" db-host "/" db-name)
     ; Any additional keys are passed to the driver
     ; as driver-specific properties.
     :user  username      ; use "root" for administration
     :password passwd}))
