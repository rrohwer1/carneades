(ns 
  ^{:doc "An implementation of the argument evaluation protocol using the 
          semantics of Carneades Argument Evaluation Structures (CAES)."}
  carneades.engine.caes
  (:use carneades.engine.statement
        carneades.engine.argument-graph
        carneades.engine.argument-evaluation))

(defrecord CEState  
  "Carneades Evaluation State"     
  [closed-statements    ; set of statement-node ids
   closed-arguments     ; set of argument-node ids
   graph])              ; argument-graph 

(defn- make-cestate [& key-values] 
  (merge (CEState. #{} #{} (make-argument-graph))
         (apply hash-map key-values)))

(defn- cestate? [x] (instance? CEState x))

(declare all-premises-holds satisfies-proof-standard?)

(defn- answer? #{:yes :no :unknown})

(defn- applicable? 
  "argument-graph argument-node -> answer
   An argument is applicable if all of its premises hold
   and it hasn't been undercut by an applicable argument.
   Returns nil if it is not known whether or not one
   of its premises hold."
  [ag an]
  (condp = (all-premises-hold? ag an)
    :yes (let [answers (map (fn [an2] (applicable? ag an2)) (undercutters ag an))]
           (cond (contains? answers :yes) :no,
                 (contains? answers :unknown) :unknown,
                 :else :yes))
    :no :no
    :unknown :unknown))
       
(defn- acceptable?
  "argument-graph statement-node -> answer"
  [ag sn]
  (satisfies-proof-standard ag sn))

(defn- statement-node-value
  "argument-graph statement-node -> nil or number in the range 0.0-1.0
   Computes the value of a statement node.  Doesn't just return its current value.
   Assumes that (acceptable? ag P) implies (not (acceptable? ag (literal-complement P))).
   This assumption holds for all well-defined proof standards, by definition."
  [ag sn]
  (condp = (acceptable? ag sn)
    :unknown nil,
    :yes 1.0,
    :no 0.0))

(defn- argument-node-value
  "argument-graph argument-node -> nil or number in the range 0.0-1.0"
  [ag an]
  (condp = (applicable? ag an) 
    :unknown nil,
    :yes 1.0,
    :no 0.0))
  
(defn- eval-statement-node 
  "cestate statement-node -> cestate
   Returns a state in which the statement node has been
   evaluated in the argument graph of the state. If the value
   of the statement node in the resulting graph is nil, then
   the argument graph contained a cycle which prevent
   the statement node from being assigned a value."
  [ces1 sn] 
  (cond (contains? (:closed-statements ces1) (:id sn)) ces1,  ; cycle!
        (:value sn) ces1,                                     ; value already assigned
        :else (let [ces2 (reduce (fn [s an] (eval-argument-node s an))
                                 (assoc ces1 
                                        :closed-statements (conj (:closed-statements ces1) 
                                                                 (:id sn)))
                                 (map (fn [id] (get-argument (:graph ces1) id))
                                      (:conclusion-of sn)))]
                (assoc ces2 
                       :graph (update-statement-node 
                                (:graph ces2) 
                                sn 
                                :value (statement-node-value (:graph ces2) sn))))))

(defn- eval-argument-node 
  "cestate argument-node -> cestate
   Returns a state in which the argument node has been
   evaluated in the argument graph of the state. If the value
   of the argument node in the resulting graph is nil, then
   the argument graph contained a cycle which prevent
   the argument node from being assigned a value."
  [ces1 an] 
  (cond (contains? (:closed-arguments ces1) (:id an)) ces1,   ; cycle!
        (:value an) ces1,                                     ; value already assigned
        :else (let [ces2 (reduce (fn [s sn] (eval-statement-node s sn))
                                 (assoc ces1 
                                        :closed-arguments (conj (:closed-arguments ces1) 
                                                                (:id an)))
                                 (map (fn [id] (get-statement (:graph ces1) id))
                                      (literal-atom (vals (:premises an)))))
                    ces3 (reduce (fn [s an2] (eval-argument-node s an2))
                                 ces2
                                 (undercutters an))]
                (assoc ces3 
                       :graph (update-argument-node 
                                (:graph ces3)
                                an
                                :value (argument-node-value (:graph ces3) sn))))))            

(defn- holds?
  "argument-graph literal -> answer
   Whether or not a literal holds depends on the value of its
   statement node.  The value is not computed here, but only read
   from the argument graph. Returns nil if there is no
   statement node for the literal in the argument graph or
   if the value of the statement node is nil."
  [ag literal]
  {:pre [(argument-graph? ag) (literal? literal)]}
  (let [sn (get (:statements ag) 
                (get (:language ag) (literal-atom literal)))]
    (cond (nil? sn) nil,
          (literal-pos? literal) 
            (cond (> (:weight sn) 0.5) :yes,  ; P assumed or accepted
                  (= (:value sn) 1.0) :yes,   ; P acceptable
                  :else :unknown),            ; unknown
          (literal-neg? literal)
            (cond (< (:weight sn) 0.5) :yes,  ; (not P) assumed or P rejected
                  (= (:value sn) 0.0) :yes,   ; (not P) acceptable
                  :else :unkown),             ; unknown
          :else :unknown)))

(defn- all-premises-hold?
  "argument-graph argument-node -> answer"
  [ag an]
  {:pre [(argument-graph? ag) (argument-node? an)]}
  (let [answers (map (fn [literal] (holds? ag literal)) 
                     (vals (:premises an)))]
    (cond (contains? answers :no) :no,
          (contains? answers :unknown) :unknown,
          :else :yes)))

;; dispatch on the proof standard
(defmulti satisfies-proof-standard? (fn [_ sn] (:standard sn)))

(defn- dv-weight [arg]
  "Dialetical validity weight. Strict arguments weigh more than defeasible arguments."
  (if (:strict arg) 2.0 1.0))

(defn- pe-weight [arg]
  "Propondernce of the evidnence weight. Strict arguments all have the
   same weight and weigh more than defeasible arguments. 
   Defeasible arguments have the weight assigned to 
   the by users, in the range 0.0 to 1.0"
  (if (:strict arg) 2.0 (:weight arg)))
  
(defn- max-dv-weight [args]
  (if (empty? args) 0.0 (apply max (map dv-weight args))))

(defn- max-pe-weight [args]
  (if (empty? args) 0.0 (apply max (map pe-weight args))))

(defmethod satisfies-proof-standard? :dv [ag sn]
  (let [app-pro (filter #(= :yes (applicable? ag %)) (:pro sn)),
        not-inapp-pro (filter #(contains? #{:yes :unknown} (applicable? ag %)) (:pro sn)),
        not-inapp-con (filter #(contains? #{:yes :unknown} (applicable? ag %)) (:con sn))]
    (cond (> (max-dv-weight app-pro) (max-dv-weight not-inapp-con)) :yes,
          (> (max-dv-weight not-inapp-pro) (max-dv-weight not-inapp-con)) :unknown,
          :else :no)))
                                     
(defmethod satisfies-proof-standard? :pe [ag sn]
  (let [app-pro (filter #(= :yes (applicable? ag %)) (:pro sn))
        not-inapp-pro (filter #(contains? #{:yes :unknown} (applicable? ag %)) (:pro sn))
        not-inapp-con (filter #(contains? #{:yes :unknown} (applicable? ag %)) (:con sn))]
    (cond (> (max-pe-weight app-pro) (max-pe-weight not-inapp-con)) :yes,
          (> (max-pe-weight not-inapp-pro) (max-pe-weight not-inapp-con)) :unknown,
          :else :no)))

;; clear-and-convincing-evidence?
(defmethod satisfies-proof-standard? :cce [ag sn]
  (let [app-pro (filter #(= :yes (applicable? ag %)) (:pro sn))
        not-inapp-pro (filter #(contains? #{:yes :unknown} (applicable? ag %)) (:pro sn))
        not-inapp-con (filter #(contains? #{:yes :unknown} (applicable? ag %)) (:con sn))
        max-app-pro (max-pe-weight app-pro),
        max-not-inapp-pro (max-pe-weight not-inapp-pro),
        max-not-inapp-con (max-pe-weight not-inapp-con),
        alpha 0.5
        beta 0.3]
    (cond (and (> max-app-pro max-not-inapp-con)
               (> max-app-pro alpha)
               (> (- max-app-pro max-not-inapp-con) beta)) :yes,
          (and (> max-not-inapp-pro max-not-inapp-con)
               (> max-not-inapp-pro alpha)
               (> (- max-not-inapp-pro max-not-inapp-con) beta)) :uknown,
          :else :no)))

;; beyond-reasonable-doubt?
(defmethod satisfies-proof-standard? :brd [ag sn]
  (let [app-pro (filter #(= :yes (applicable? ag %)) (:pro sn)),
        not-inapp-pro (filter #(contains? #{:yes :unknown} (applicable? ag %)) (:pro sn)),
        not-inapp-con (filter #(contains? #{:yes :unknown} (applicable? ag %)) (:con sn)),
        max-app-pro (max-pe-weight app-pro),
        max-not-inapp-con (max-pe-weight not-inapp-con),
        alpha 0.5,
        beta 0.5,
        gamma 0.2]
    (cond (and (> max-app-pro max-not-inapp-con) 
               (> max-app-pro alpha)
               (> (- max-app-pro max-not-inapp-con) beta)
               (< max-not-inapp-con gamma)) :yes,
          (and (> max-not-inapp-pro max-not-inapp-con) 
               (> max-not-inapp-pro alpha)
               (> (- max-not-inapp-pro max-not-inapp-con) beta)
               (< max-not-inapp-con gamma)) :unknown,
          :else :no)))

(defn- evaluate-argument-graph
  [ag]
  (:graph (reduce (fn [ces sn] (evaluate-statement-node ces sn))
                  (make-cestate :graph ag)
                  (vals (:statement-nodes ag)))))

(def carneades-evaluator 
  (reify ArgumentEvaluator
    (evaluate [this ag] (evaluate-argument-graph (reset-node-values ag)))
    (label [this node] (standard-label node))))
         
         
         
         