(ns 
  ^{:doc "An atomic argument represents a single inference step.
          The premises and conclusion of the argument are statements,
          representing propositional or predicate logic literals.
          The arguments need not be fully instantiated; the premises
          and conclusion of the argument may contain free variables."}
  
  carneades.engine.atomic-argument
  (:use carneades.engine.statement
        carneades.engine.unify))

(defrecord AtomicArgument
  [id               ; symbol
   title            ; string or hash table (for multiple languages)
   scheme           ; string
   strict           ; boolean
   weight           ; real number between 0.0 and 1.0, default 0.5
   conclusion       ; statement
   premises         ; (string -> statement) map, where strings are role names 
   sources])        ; vector of source texts
  

 (defn make-atomic-argument
   "Makes an atomic argument. A vector of statements may be
    supplied as the value of the :premises property, instead of 
    a map from role names to statements. In this case the premises
    are assigned integer roles names, based on the order of the
    premises in the vector."
    [& values]
    (let [m (apply hash-map values)
      arg (merge (AtomicArgument. 
               (gensym "a") ; id
               ""           ; title
               ""           ; 
               false        ; strict
               0.5          ; weight
               nil          ; conclusion
               {}           ; premises
               [])       ; sources 
        m)]
      (if (instance? clojure.lang.PersistentArrayMap (:premises arg))
        arg
        (assoc arg :premises 
               (zipmap (map str (range (count (:premises arg)))) 
                       (:premises arg))))))
     

(defn argument-variables
  "arg -> (seq-of symbol)
   Returns a seq containing the variables of the atomic argument arg"
  [arg]
  (distinct (concat (mapcat #(variables (:atom %)) (vals (:premises arg)))
                    (variables (:conclusion arg)))))

(defn instantiate-argument
  "argument substitutions -> arg
   Instantiate the variables of an atomic argument by applying substitions"
  [arg subs]
  (assoc arg
         :id (gensym "a")
         :premises (zipmap (keys (:premises arg)) 
                           (map (fn [a] (apply-substitutions subs a)) 
                                (vals (:premises arg))))
         :conclusion (apply-substitutions subs (:conclusion arg))))

