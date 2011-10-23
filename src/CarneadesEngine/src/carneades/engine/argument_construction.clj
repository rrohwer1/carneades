(ns ^{:doc "Argument construction using generators."}
  carneades.engine.argument-construction
  (:use clojure.set
        clojure.pprint
        carneades.engine.statement
        carneades.engine.unify
        carneades.engine.argument-graph
        carneades.engine.atomic-argument
        carneades.engine.argument-generator
        carneades.engine.argument-builtins
        carneades.engine.argument-from-arguments))

(defrecord ArgumentTemplate 
  [guard       ; term with all unbound variables of the argument
   instances   ; set of ground terms matching the guard
   argument])  ; atomic argument

(defn make-argument-template
   [& values]
   (let [m (apply hash-map values)]
   (merge (ArgumentTemplate. 
             nil    ; guared
             #{}    ; instances
             nil)   ; atomic argument
          m)))

(defrecord Goal
  [issues         ; (seq-of statement)
   substitutions  ; (term -> term) map
   depth])        ; int  

(defn make-goal
   [& values]
   (let [m (apply hash-map values)]
   (merge (Goal. 
             ()     ; issues
             {}     ; substitutions
             0)     ; depth
          m)))

(defrecord ACState  
  "argument construction state"     
  [goals                  ; (symbol -> goal) map, where the symbols are goal ids
   open-goals             ; set of goal ids (todo: change to a priority queue)
   closed-issues          ; set of statements for goals already processed 
   graph                  ; argument-graph 
   arg-templates          ; (symbol -> argument template) map; symbols are template ids
   asm-templates])        ; vector of non-ground statements

(defn make-acstate
  [& values]
  (let [m (apply hash-map values)]
    (merge (ACState. 
             {}     ; goals
             #{}    ; open goals
             #{}    ; closed-issues
             (make-argument-graph) 
             {}     ; argument templates
             [])    ; assumption templates
           m)))

(defn initial-acstate
  "statement argument-graph -> ac-state"
  [issue ag]
  (let [goal-id (gensym "g")]
    (make-acstate
      :goals {goal-id (make-goal :issues (list issue) 
                                 :substitutions {} 
                                 :depth 0)}
      :open-goals #{goal-id}
      :graph ag)))


(defn- add-goal
  [state1 goal]
  ; (println "add-goal")
  ; (pprint goal)
  (let [id (gensym "g")]
    (assoc state1
           :goals (assoc (:goals state1) id goal)
           :open-goals (conj (:open-goals state1) id))))
  
(defn- update-issues
  "ac-state goal response -> ac-state
   Add a goal to the state by replacing the first issue of the parent goal
   with the given issues. The depth of the parent goal is incremented in this new goal."
  [state1 g1 response]
  ; (println "process premises")
  ; (pprint (map premise-statement premises))
  (add-goal state1 
            (make-goal 
              ; pop the first issue and add issues for the
              ; premises of the argument to the beginning for
              ; depth-first search
              :issues (concat (map premise-statement 
                                   (:premises (:argument response)))
                              (rest (:issues g1)))
              :substitutions (:substitutions response)
              :depth (inc (:depth g1)))))

(defn- process-conclusion
  "ac-state goal substitutions statement -> ac-state
   To enable the construction of rebuttals, add the complement of the conclusion 
   to the goals of the ac-state, unless it is a closed issue. Note: The depth of 
   the goal for the rebuttal is the same as the depth of the goal being rebutted, 
   not incremented, so that if goals are prioritized by depth complementary goals
   will get the same priority."
  [state1 g1 subs statement] 
  ; (println "process-conclusion")
  (let [stmt (apply-substitutions subs (statement-complement statement))]
    (if (contains? (:closed-issues state1) stmt)
      state1
      (add-goal state1 (make-goal 
                         :issues (list stmt)
                         :substitutions subs
                         :depth (:depth g1))))))
                
  
(defn- process-argument
  "ac-state goal substitutions argument -> ac-state"
  [state1 goal response]
  ; (println "process-argument:") 
  (let [subs (:substitutions response)
        arg (:argument response)]
    (if (not arg)
      state1
      (-> state1
          ; (process-premises goal subs (:premises arg))
          ; premises have already been processed by update-issues
          (process-conclusion goal subs (:conclusion arg))
          ; to do: add a goal for excluding the rule/scheme, for undercutters
          (assoc :arg-templates (assoc (:arg-templates state1) 
                                       (gensym "at") 
                                       (make-argument-template
                                         :guard `(~'guard ~@(argument-variables arg))
                                         :instances #{}
                                         :argument arg)))))))
(defn- add-instance
  "map symbol term -> map"
  [arg-template-map key term]
  ; (pprint "add instance")
  (let [arg-template (get arg-template-map key)]
    (assoc arg-template-map key 
           (assoc arg-template 
                  :instances (conj (:instances arg-template) term)))))
  
(defn- apply-arg-templates
  "ac-state response -> ac-state
   Apply the argument templates to the substitutions of the response, adding
   arguments to the argument graph of the ac-state for all templates with ground
   guards, if the instance is new.  Add the new instance to the set of instances 
   of the template."
  [state1 response]
  ; (pprint "apply-arg-templates state: ")
  (let [subs (:substitutions response)]
    (reduce (fn [s k]
              (let [template (get (:arg-templates s) k)
                    trm (apply-substitutions subs (:guard template))]
                ;  (println "template: " template)
                ;  (println "term: " trm)
                (if (or (not (ground? trm))
                        (contains? (:instances template) trm))
                  s
                  (let [arg (instantiate-argument (:argument template) subs)]
                    ; (pprint arg)
                    (assoc s 
                           :graph (assert-argument (:graph s) arg)
                           :arg-templates (add-instance (:arg-templates s) k trm))))))
            state1
            (keys (:arg-templates state1)))))

(defn- apply-asm-templates
  "ac-state goal substitutions -> ac-state"
  [state1 g1 subs]
  ; (println "state1:" state1)
  ; (println "asm-templates: ")
  ; (pprint (:asm-templates state1))
  (reduce (fn [state2 template] 
            (let [ag (:graph state2)
                  asm (apply-substitutions subs template)]
              ; (println "asm: " asm)
              (if (not (ground? asm))
                state2
                (let [ag2 (condp = (status ag asm)
                            :stated (accept ag [asm])
                            :questioned ag
                            :rejected (question ag [asm])
                            :accepted ag)]
                  (add-goal (assoc state2 :graph ag2)
                            (make-goal 
                              :issues (list (statement-complement asm))
                              :substitutions subs
                              :depth (inc (:depth g1))))))))
          state1
          (:asm-templates state1)))      

(defn- process-assumptions
  "ac-state response -> ac-state
   add the assumptions of the response to the assumption templates"
  [state response]
  (let [subs (:substitutions response)
        asms (:assumptions response)]
    (assoc state :asm-templates 
           (concat (:asm-templates state) 
                   (map (fn [asm] (apply-substitutions subs asm))
                        asms)))))
         
(defn- apply-response
  "ac-state goal response -> ac-state"
  [state1 goal response]
  ; (pprint {:response response})
  (-> state1
      (update-issues goal response)
      (process-argument goal response)
      (process-assumptions response) 
      (apply-arg-templates response)
      (apply-asm-templates goal (:substitutions response))))

(defn select-random-member
  "set -> any
   Select and return a random member of a set"
  [set] 
  (let [sq (seq set)] 
    (nth sq (rand-int (count sq)))))

(defn- generate-substitutions-from-assumptions
  "argument-graph -> argument-generator"
  [ag1]
  (reify ArgumentGenerator
    (generate [goal subs]
              (reduce (fn [l stmt]
                        (let [subs2 (unify goal stmt subs)]
                          (if (not subs2)
                            l
                            (conj l (make-response subs2 #{} nil)))))
                      []
                      (assumptions ag1)))))

(defn- reduce-goal
  "ac-state symbol generators -> ac-state
   reduce the goal with the given id remove it from from the goal lists"
  [state1 id generators1]
  ; (pprint "reduce-goal")
  (let [goal (get (:goals state1) id)  
        ; remove the goal from the state
        state2 (assoc state1    
                      :goals (dissoc (:goals state1) id)
                      :open-goals (disj (:open-goals state1) id))]               
    
    (if (empty? (:issues goal))
      state2             
      (let [issue (first (:issues goal))]
        ; (print "goal: ")
        ; (pprint goal)
        (if (contains? (:closed-issues state2) issue)
          state2
          (let [state3 (assoc state2 :closed-issues (conj (:closed-issues state2) issue))
                generators2 (concat 
                              (list (generate-substitutions-from-assumptions (:graph state3)))
                              generators1)]
            ; (println "issue: " issue)
            ; (println "sissue: " (apply-substitutions (:substitutions goal) issue))
            ; apply the generators to the selected issue
            (let [responses (apply concat 
                                   (map (fn [g] 
                                          (generate g issue (:substitutions goal))) 
                                        generators2))]
              ; (println "responses: " (count responses))
              (reduce (fn [s r] (apply-response s goal r))
                      state3
                      responses))))))))
    
(defn- reduce-goals
  "ac-state integer (seq-of generator) -> ac-state
   Construct arguments for both viewpoints and combine the arguments into
   a single argument graph of a ac-state.  All arguments found within the given
   resource limits are included in the argument graph of the resulting ac-state."
  [state1 max-goals generators]
  ; (pprint "reduce-goals")
  (let [id (select-random-member (:open-goals state1))]     
    (if (or (not id) (<= max-goals 0))
      state1 
      (recur (reduce-goal state1 id generators) 
             (dec max-goals) 
             generators))))

(defn construct-arguments
  "argument-graph statement int (set-of statement) (seq-of generator) -> argument-graph
   Construct an argument graph for both sides of an issue."
  ([ag1 issue max-goals assumptions generators1]
    ; (pprint "argue")
    (let [ag2 (accept ag1 assumptions)
          generators2 (concat (list (builtins))  generators1)]
      (:graph (reduce-goals (initial-acstate issue ag2) 
                            max-goals 
                            generators2))))
  ([issue max-goals assumptions generators]
    (let [ag (make-argument-graph :main-issue issue)]
      (construct-arguments ag max-goals assumptions generators))))
