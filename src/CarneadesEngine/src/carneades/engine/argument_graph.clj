(ns carneades.engine.argument-graph
 (:use clojure.pprint
       carneades.engine.statement
       carneades.engine.dublin-core
       carneades.engine.argument))


; A literal is a propositional letter, represented by a symbol, 
; or the negation of a propositional letter.

(defn- make-literal
  [positive letter]
  (if positive letter (list 'not letter)))

(defrecord ArgumentNode
  [id               ; symbol
   title            ; string or hash table (for multiple languages)
   scheme           ; string
   strict           ; boolean
   weight           ; 0.0-1.0, default 0.5; input to argument evaluation
   value            ; nil or 0.0-1.0, default nil; output from argument evaluation
   conclusion       ; literal
   premises         ; (string -> literal) map, where the keys are role names
   sources])        ; vector of dublin-core metadata about the sources of the argument

(defn- make-argument-node
   "key value ... -> argument-node"
   [& key-values]
   (merge (ArgumentNode. 
            (gensym "a") ; id
            ""           ; title
            ""           ; scheme
            false        ; strict
            0.5          ; weight
            nil          ; value
            nil          ; conclusion
            {}           ; premises
            [])          ; sources 
          (apply hash-map key-values)))

(defn argument-node? [x] (instance? ArgumentNode x))
  
(defn proof-standard?
  [k]
  (contains? #{:dv, :pe, :cce, :brd} k))

(defrecord Poll
  [agree        ; integer
   disagree     ; integer
   no-opinion]) ; integer

(defn poll? [x] (instance? Poll x))

(defn make-poll 
  "key value ... -> poll"
   [& key-values]  
   (merge (Poll. 
            0   ; id
            0   ; title
            0)  ; main-issue
          (apply hash-map key-values)))

; type language = :en | :de | :nl | :fr ...

; Note: the atom of a statement node must be kept in sync with the atom associated
; with the id of the statement node in the language table, i.e. the key list.

(defrecord StatementNode
  [id               ; symbol, same as the propositional letter in the key list
   atom             ; ground atomic formula or nil
   weight           ; nil or 0.0-1.0, default nil; input to argument evaluation
   value            ; nil or 0.0-1.0, default nil; outut from argument evaluation
   poll             ; nil or poll
   standard         ; proof-standard
   text             ; (language -> string) map, natural language formulations of the statement
   premise-of       ; (set-of symbol), argument node ids
   pro              ; (set-of symbol), pro argument node ids
   con])            ; (set-of symbol), con argument node ids
 
(defn- make-statement-node
  [stmt]
  {:pre [(statement? stmt)]}
  (StatementNode. (gensym "s")       ; id
                  (:atom stmt)        
                  (:weight stmt)    
                  nil                ; value   
                  nil                ; poll                  
                  (:standard stmt)
                  (:text stmt)
                  #{}                ; premise-of
                  #{}                ; pro argument node ids
                  #{}))              ; con argument node ids

(defn statement-node? [x] (instance? StatementNode x))

   
(defrecord ArgumentGraph 
  [id               ; symbol
   title            ; string or hash table (for multiple languages)
   main-issue       ; symbol, a key into the statement node map
   language         ; (sexp -> symbol) map, i.e. a "key list"; 
                    ; where the sexp represents a ground atomic formula
   statement-nodes  ; (symbol -> StatementNode) map, 
   argument-nodes   ; (symbol -> ArgumentNode) map
   references])     ; (symbol -> Metadata) map

(defn make-argument-graph
   "key value ... -> argument-graph"
   [& key-values]  
   (merge (ArgumentGraph. 
            (gensym "ag")   ; id
            ""              ; title
            nil             ; main-issue
            {}              ; keys
            {}              ; statement nodes
            {}              ; argument nodes
            {})             ; references to sources
          (apply hash-map key-values)))

(defn argument-graph? [x] (instance? ArgumentGraph x))

(defn get-argument-node
  "argument-graph symbol -> argument-node | nil"
  [ag id]
  (get (:argument-nodes ag) id))

(defn pro-argument-nodes
  "argument-graph statement-node -> (seq-of argument-node)"
  [ag sn]
  (map (fn [id] (get-argument-node ag id))
       (:pro sn)))

(defn con-argument-nodes
  "argument-graph statement-node -> (seq-of argument-node)"
  [ag sn]
    (map (fn [id] (get-argument-node ag id))
       (:con sn)))

(defn get-statement-node  
  "argument-graph statement -> statement-node or nil
  Returns the statement node for a statement, or nil
  if no statement node for the statement exists. Use
  create-statement-node instead to create a statement
  node if one doesn't yet exist."
  [ag stmt]
  {:pre [(argument-graph? ag) (statement? stmt)]}
  (get (:statement-nodes ag) 
       (get (:language ag) (:atom stmt))))
  
(defn- create-statement-node
  "argument-graph statement -> [argument-graph statement-node]
   Returns a [argument-node statement node] pair for the statement, 
   creating one if one does not exist in the initial argument-graph."
  [ag stmt]
  {:pre [(argument-graph? ag) (statement? stmt)]}
  (let [n (get (:statement-nodes ag) 
               (get (:language ag) (:atom stmt)))]
    (if n 
      [ag n]
      (let [n2 (make-statement-node stmt)
            ag2 (assoc ag 
                       :language 
                       (assoc (:language ag)
                              (:atom stmt)
                              (:id n2))
                       :statement-nodes 
                       (assoc (:statement-nodes ag)
                              (:id n2)
                              n2))]
        [ag2 n2]))))

(defn update-statement-node
  "argument-graph statement-node key value ... -> argument-graph
   Updates the statement node with the values of the 
   properties with the given keys, but retaining the values of other properties.
   Warning: this is a low level function. It does not (yet) keep the atom of the statement
   in sync with its key in the language table."
  [ag node & key-values]
  {:pre [(argument-graph? ag) (statement-node? node)]}
  ; (println "node: " node)
  (assoc ag 
         :statement-nodes (assoc (:statement-nodes ag)
                                 (:id node)
                                 (merge node (apply hash-map key-values)))))

(defn- get-statement-literal
  "argument-graph statement -> literal
   Precondition: the atom of the literal has already been entered into the 
   language table (key list) of the argument graph." 
  [ag stmt]
  {:pre [(not (nil? (get (:language ag) (:atom stmt))))]}
  (make-literal (statement-pos? stmt)
                (get (:language ag) (:atom stmt))))


   
(defn- link-conclusion
  [ag literal arg-id]
  {:pre [(not (nil? (get (:statement-nodes ag) (literal-atom literal))))]}
  (let [n (get (:statement-nodes ag) (literal-atom literal))]
    (if (literal-pos? literal)  ; then conclusion of a pro argument
      (update-statement-node 
        ag 
        n
        :pro (conj (:pro n) arg-id))
      (update-statement-node
        ag
        n 
        :con (conj (:con n) arg-id)))))

(defn- link-premises
  [ag1 literals arg-id]
  (reduce (fn [ag2 literal]
            (let [n (get (:statement-nodes ag2) (literal-atom literal))]
              (update-statement-node 
                ag2 
                n
                :premise-of (conj (:premise-of n) arg-id))))
          ag1
          literals))

(defn- find-sources
  "argument-graph (seq-of string) -> (seq-of source)
   Returns the sources with the given ids in the
   reference list of the argument graph."
  [ag ids]
  (concat (map (fn [id] (get (:references ag) id)) ids)))
                          
(defn- update-references
  "argument-graph source -> argument-graph
   Checks whether a source is in the list of references of the
   argument graph and updates the list to add the source if it
   is new. Does not overwrite or modify existing references."
  [ag source]
  {:pre (not (nil? (first (:identifier source))))}
  (let [source2 (get (:references ag) 
                     (first (find-sources (:identifier source))))]
    (if source2
      ; the source was already in the reference list
      ag 
      ; else add the new source to the reference list
      (assoc ag :references
             (assoc (:references ag)
                    (first (:identifier source))
                    source)))))

(defn- source-ids
  "argument-graph (seq-of source) -> (seq-of string)
   Returns the identifiers used to identifty each source in the list 
   of references of the argument map."
  [ag sources]
  (filter (fn [x] (not (nil? x)))
          (map (fn [src] 
                 (first (map (fn [id] 
                               (get (:references ag) id))
                             (:identifier src))))
               sources)))

(defn- add-argument-node
  "argument-graph argument-node -> argument-graph"
  [ag node]
  (assoc ag :argument-nodes (assoc (:argument-nodes ag) (:id node) node)))

(defn  update-argument-node
  "argument-graph argument-node key value ... -> argument-graph
   Updates the argument node, replacing the properties with
   the given keys values but retaining the values of other properties.
   Warning: this is a low level function. It does not (yet) keep the premises
   and conclusion of the argument in sync with the premise-of and pro or con
   properties of statements in the statement table of the argument graph."
  [ag node & key-values]
  {:pre [(argument-graph? ag) (argument-node? node)]}
  (assoc ag 
         :argument-nodes (assoc (:argument-nodes ag)
                                (:id node)
                                (merge node 
                                       (apply hash-map key-values)))))


(defn assert-argument
  "argument-graph argument -> argument-graph
   Converts a one-step argument to an argument node and adds
   it to the argument graph. Precondition: statement nodes 
   have already been created in the argument graph for all 
   the statements in the argument."
  [ag1 arg]
  {:pre [(ground? (:conclusion arg)) 
         (every? ground? (vals (:premises arg)))]}
  ; (pprint {:arg arg})
  (let [ag2 (reduce (fn [ag stmt] (first (create-statement-node ag stmt)))
                    ag1
                    (conj (vals (:premises arg)) (:conclusion arg)))
        ag3 (reduce (fn [ag src] (update-references ag src))
                    ag2
                    (:sources arg))
        node (make-argument-node 
               :id (:id arg)           
               :title (:title arg)      
               :scheme (:scheme arg) 
               :strict (:strict arg)
               :weight (:weight arg)  
               :conclusion (get-statement-literal ag3 (:conclusion arg))
               :premises (zipmap (keys (:premises arg)) 
                                 (map (fn [p] (get-statement-literal ag3 p)) 
                                      (vals (:premises arg)))) 
               :sources (source-ids ag3 (:sources arg)))]
    (-> ag3 
        (add-argument-node node)
        (link-conclusion (:conclusion node) (:id arg))
        (link-premises (vals (:premises node)) (:id arg)))))

(defn assert-arguments
  "argument-graph (collection-of argument) -> argument-graph"
  [ag args]
  {:pre [(not (nil? ag))]}
  (reduce (fn [ag arg] (assert-argument ag arg)) ag args))


(defn assoc-standard
  "argument-graph  proof-standard (list-of statement) -> argument-graph
   Assigns the given proof standard to each statement in the list, creating
   statement nodes for statements without nodes in the argument graph."
  [ag ps statements]
  (reduce (fn [ag stmt]
            (let [[ag2 n] (create-statement-node ag stmt)]
              (assoc ag2 :statement-nodes (assoc (:statement-nodes ag2)
                                                 (:id n) 
                                                 (assoc n :standard ps)))))
          ag statements))


(defn arguments 
  "argument-graph [statement] -> (seq-of argument-node)
   Returns all argument nodes in an argument graph pro and con some statement,
   or all argument nodes in the argument graph, if no statement is provided."
  ([ag stmt]
    (let [sn (get-statement-node ag stmt)]  
      (if (nil? sn)
        ()
        (map (fn [arg-id] (get (:argument-nodes ag) arg-id))
             (concat (:pro sn) (:con sn))))))          
  ([ag]
    (if-let [args (vals (:argument-nodes ag))]
      args
      ())))

(defn pro-arguments
  "argument-graph statement -> (seq-of argument-node)"
  [ag s]
  (filter (fn [node]
            (= (statement-pos? s) 
               (literal-pos? (:conclusion node))))
          (arguments ag s)))

(defn con-arguments
  "argument-graph statement -> (seq-of argument-node)"
  [ag s]
  (pro-arguments ag (statement-complement s)))

(defn undercutters
  "argument-graph argument-node -> (seq-of argument-node)"
  [ag an]
  (let [atom `(~'undercut ~(:id an))
        sn (get-statement-node ag (make-statement :atom atom))]
    (if (nil? sn)
      ()
      (map (fn [an-id] (get (:argument-nodes ag) an-id))
           (:pro sn)))))
   
(defn schemes-applied
  "argument-graph statement -> (seq-of string)"
  [ag stmt]
  (map :scheme (arguments ag stmt)))

(defn stated?
  [ag s]
  (nil? (:weight (get-statement-node ag s))))

(defn accept 
  "argument-graph (seq-of statement) -> argument-graph"
  [ag stmts]
  {:pre [(argument-graph? ag) 
         (every? statement? stmts)]}
  ; (println "stmts: " stmts)
  (reduce (fn [ag2 stmt]
            (let [[ag3 sn] (create-statement-node ag2 stmt)]
              (update-statement-node 
                ag3 
                sn
                :weight (if (statement-pos? stmt) 1.0 0.0))))
          ag 
          stmts))
   
(defn accepted?
  "argument-graph statement -> boolean"
  [ag s]
  (let [n (get-statement-node ag s)]
    (if (statement-pos? s)
      (= (:weight n) 1.0)
      (= (:weight n) 0.0))))

(defn accepted-statements
  "argument-graph -> (seq-of statement)
   Returns a sequence of the accepted statements in the argument
   graph. If a statement P is rejected in the graph, its complement
   (not P) is accepted and included in the resulting sequence."
  [ag]
  (reduce (fn [s n] (cond (= (:weight n) 1.0) (conj s (:atom n))
                          (= (:weight n) 0.0) (conj s (statement-complement (:atom n)))
                          :else s))
          ()
          (:statement-nodes ag)))

(defn facts
  "argument-graph -> (seq-of statement)
   Returns the accepted statements of the argument graph. A
   synonym for the accepted-statements function."
  [ag]
  (accepted-statements ag))
  
(defn reject 
  "argument-graph (seq-of statement) -> argument-graph"
  [ag stmts]
  (reduce (fn [ag2 stmt]
            (let [[ag3 sn] (create-statement-node ag2 stmt)]
              (update-statement-node 
                ag3 
                sn
                :weight (if (statement-pos? stmt) 0.0 1.0))))
          ag 
          stmts))

(defn rejected?
  "argument-graph statement -> boolean"
  [ag s]
  (let [n (get-statement-node ag s)]
    (if (statement-pos? s)
      (= (:status n) 0.0)
      (= (:status n) 1.0))))

(defn rejected-statements
  "argument-graph -> (seq-of statement)
   Returns a sequence of the rejected statements in the argument
   graph. If a statement P is accepted in the graph, its complement
   (not P) is rejected and included in the resulting sequence."
  [ag]
  (reduce (fn [s n] (cond (= (:weight n) 0.0) (conj s (:atom n))
                          (= (:weight n) 1.0) (conj s (statement-complement (:atom n)))
                          :else s))
          ()
          (:statement-nodes ag)))

(defn assume 
  "argument-graph (seq-of statement) -> argument-graph"
  [ag stmts]
  (println "assume stmts: " stmts)
  (reduce (fn [ag2 stmt]
            (let [[ag3 sn] (create-statement-node ag2 stmt)]
              (update-statement-node 
                ag3 
                sn
                :weight (if (statement-pos? stmt) 0.75 0.25))))
          ag 
          stmts))
                           
                              
(defn assumed?
  "argument-graph statement -> boolean"
  [ag s]
  (let [x (:weight (get-statement-node ag s))]
    (if (nil? x)
      false
      (if (statement-pos? s)
        (< 0.5 x 1.0)
        (< 0.0 x 0.5)))))

(defn assumptions
  "argument-graph -> (seq-of statement)
   Returns a sequence of the assumptions in the argument
   graph."
  [ag]
  (reduce (fn [s n] (cond (< 0.5 (:weight n) 1.0) (conj s (:atom n))
                          (< 0.0 (:weight n) 0.5) (conj s (statement-complement (:atom n)))
                          :else s))
          ()
          (:statement-nodes ag)))

(defn question 
  "argument-graph (seq-of statement) -> argument-graph"
  [ag stmts]
  (reduce (fn [ag2 stmt]
            (let [[ag3 sn] (create-statement-node ag2 stmt)]
              (update-statement-node 
                ag3
                sn
                :weight 0.5)))
          ag 
          stmts))
  
(defn issue?
  "argument-graph statement -> boolean"
  [ag s]
  (let [x (:weight (get-statement-node ag s))]
    (and (<= x 0.75)
         (>= x 0.25))))

(defn issues
  "argument-graph -> (seq-of statement)
   Returns a sequence of the issues in the argument
   graph. Only positive statements are returned. 
   ?P is an issue iff P is an issue."
  [ag]
  (reduce (fn [s n] (if (= (:weight n) 0.5) (conj s (:atom n)) s))
          ()
          (:statement-nodes ag)))

(defn atomic-statements 
  "argument-graph -> (seq-of statement)
   Returns a sequence of the atomic statements in the argument graph."
  [ag]
  (map (fn [n] (:statement n))
       (vals (:statement-nodes ag))))


 (defn reset-node-values
   "argument-graph -> argument-graph
    Resets the values of argument and statement nodes to nil in 
    an argument graph, to assure that they are reevaluated."
   [ag]
   {:pre [(argument-graph? ag)]}
   (letfn [(reset-arguments [ag] (reduce (fn [ag an] (update-argument-node ag an :value nil))
                                         ag
                                         (vals (:argument-nodes ag))))
           (reset-statements [ag] (reduce (fn [ag sn] (update-statement-node ag sn :value nil))
                                          ag
                                          (vals (:statement-nodes ag))))]
     (-> ag
         (reset-arguments)
         (reset-statements))))


          
     
     
     


