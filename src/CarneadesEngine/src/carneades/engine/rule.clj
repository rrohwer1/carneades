;;; Copyright © 2010 Fraunhofer Gesellschaft
;;; Licensed under the EUPL V.1.1


(ns carneades.engine.rule
  (:use clojure.contrib.def
        clojure.contrib.pprint
        [clojure.set :only (intersection)]
        carneades.engine.utils
        carneades.engine.argument
        carneades.engine.statement
        [carneades.engine.dnf :only (to-dnf)]
        [carneades.engine.unify :only (genvar unify rename-variables)])
  (:require [carneades.engine.argument-search :as as]))


;; This is an implementation of the argumentation scheme for
;; for arguments from defeasible rules.  Rules may have multiple
;; conclusions, as in SWRL, and be subject to exceptions. Priorities are used
;; to resolve conflicts among rules.  Unlike in SWRL, compound terms
;; are allowed in statements.  That is, this rule language is not restricted
;; to "datalog".  Indeed, datalog may not be sufficient for modeling
;; legal rules.  For example, we need to be able to reason about whether
;; some statement hold at some time, using fluents in the event calculus.
;; And we need to reason about the applicability of rules to statements.


;; Negation, exceptions and assumptions. Statements of the form (not P),
;; (unless P) and (assuming P) have special meaning.

;; Goals of the form (not P) may occur in the head
;; rules, to allow rules to be used to generate con arguments.
;; Since multiple atoms may be in the head of a rule, this
;; approach allows a single rule to generate both pro and con arguments.

;; The (not P), (unless P) and (assuming P) forms may occur
;; in the body of a rule.  These cause negations, exceptions and assumptions,
;; respectively, to be included in the premises of the arguments generated by
;; the rule.

;; The form (not P) has a slightly different meaning when used in the head than
;; in the body of a rule.  In the head, it means the rule can be used
;; to generate an argument con P. In the body (not P) means dialectical,
;; not classical negation. (not P) is satisifed if the *complement of the
;; proof standard* for P is met by P.  Where the complement of some proof
;; standard is constructed by reversing the roles of pro and con arguments in
;; the standard. For example, the complement of SE is satisified iff there is a
;; least one defensible con argument.  And the complement
;; of DV is satisfied iff there is at least one defensible
;; con argument and no defensible pro arguments.

;; The form (unless P) is a weaker form of negation than (not P).
;; Whereas (unless P) holds if P is not acceptable, (not P) holds only if
;; P is rejectable.

;; <condition> := <statement> | (unless <statement>) | (assuming <statement>)

;; type clause = (list-of condition) ; representing a *conjunction* of
;; conditions

;; Predicates with special meaning for rules:
;; (applies <symbol> <statement>)
;; (excluded <symbol> <statement>)
;; (rebuts <symbol> <symbol> <statement>)

(defn condition-statement [c]
  "condition -> statement

   the statement of a condition"
  (let [predicate (first c)
        statement (second c)]
    (condp = predicate
      'unless statement
      'assuming statement
      c)))

;; TO DO: represent the roles of conditions, e.g. "major", "minor"

;; predicate: (statement | condition) -> symbol
;; By convention, the "predicate" of literal sentences, e.g. represented
;; as a string or symbol, is the sentence itself.

(defn predicate [s]
  (if (seq? s)
  (statement-predicate (let [pred (first s)
                             stmt (second s)]
                         (condp = pred
                           'not stmt
                           'unless stmt
                           'assuming stmt
                           'applies (nth s 2)
                           s)))
    (do
      (println "predicate - no seq! :" s)
      s)))

(defstruct named-clause
  :id ;; symbol
  :rule ;; rule-id
  :strict ;; rule-strict?
  :head ;; rule-head
  :clause ;; the actual clause
  )

(defstruct- rule-struct
  :id ;; symbol
  :strict ;; boolean, critical questions apply only if this is #f
  :head   ;; (seq-of statement), allow multiple conclusions
  :body ;; (seq-of clause)
  ;; disjunction of conjunctions, i.e. disjunctive normal form
  )

(defn make-rule [& keysvals]
  (apply struct rule-struct keysvals))

;; Note: A strict rule is still defeasible in this model.
;; A strict rule is simply a rule for
;; which the usual critical questions about rules do not apply
;; and may not be asked.

(defn make-rule-head [expr]
  "expression -> (seq-of statement)"
  (if (and (seq? expr) (= (first expr) 'and))
    (rest expr)
    (list expr)))

(defn make-rule-body [expr]
   "expr -> (seq-of clause)"
   (letfn [(process-disjunct [expr]
                             (if (seq? expr)
                               (if (= (first expr) 'and)
                                 (rest expr)
                                 (list expr))
                               (list expr)))]
     (let [dnf (to-dnf expr)]
       (cond (and (seq? dnf) (= (first dnf) 'and))
             (list (rest dnf))
             (and (seq? dnf) (= (first dnf) 'or))
             (map process-disjunct (rest dnf))
             ;; single condition
             :else (list (list dnf))))))

(defmacro assertion [id conclusion]
  `(make-rule '~id false '(~conclusion)))

(defmacro assertions [id & conclusions]
  `(make-rule '~id false '(~@conclusions)))

(defmacro assertion* [id conclusion]
  `(make-rule '~id true '(~conclusion)))

(defmacro assertions* [id & conclusions]
  `(make-rule '~id true '(~@conclusions)))

(defn- rule-macro-helper [id body strict]
  (if (not (empty? body))
    (let [[ifsymbol conditions conclusions] body]
      (if (= ifsymbol 'if)
        `(make-rule '~id ~strict
                    '~(make-rule-head conclusions)
                    '~(make-rule-body conditions))
        (throw (IllegalArgumentException.
                (format "Invalid symbol \"%s\", expected \"if\" "
                        ifsymbol)))))
    (throw (IllegalArgumentException.
            "Empty sequence as second argument"))))

(defmacro rule  [id body]
  "create a rule, not strict"
  (rule-macro-helper id body false))

(defmacro rule*  [id body]
  "create a strict rule"
  (rule-macro-helper id body true))

(defn statement-to-premise [s]
  (if (seq? s)
    (let [[predicate stmt] s]
      (condp = predicate
        'unless (ex stmt)
        'assuming (am stmt)
        (pm s)))
    (pm s)))

(defvar *question-types* #{'excluded 'priority 'valid}
  "question-type = excluded | priority | valid")

(defn rule-critical-questions [rid qs s strict]
  "rule-id (seq-of question-type) statement bool -> (seq-of premise)

   The critical questions for an argument about a statement s
   generated from a rule r:
   1) Is r a valid rule?
   2) Is r excluded with respect to s?
   3) Is there another rule of higher priority which rebuts r?
   "
  (letfn [(questionfilter [question]
                          (condp = question
                            'excluded
                            (ex (struct fatom
                                        "Rule %s is excluded for %s."
                                        `(~'excluded ~rid ~s)))
                            'priority
                            (ex
                             (struct fatom
                              "Rule %s has priority over %s respect to %s."
                              `(~'priority ~(genvar) ~rid ~s)))
                            'valid
                            (ex `(~'not ~(struct fatom "Rule %s is valid."
                                                 `(~'valid ~rid))))))]
    (if strict
      '()
      ;; filter out unknown questions
      (map questionfilter (intersection (set qs) *question-types*)))))

(defn- clause-x [cl type]
  (map condition-statement (filter #(= (first %) type) cl)))

(defn clause-exceptions [cl]
   "clause -> (seq-of statement)

    The exceptions in a clause"
   (clause-x cl 'unless))

(defn clause-assumptions [cl]
 "clause -> (list-of statement)

  The assumptions in a clause"
  (clause-x cl 'assuming))

(defn rename-rule-variables [r]
  (let [[m head] (rename-variables {} (:head r))
        [m2 body] (rename-variables m (:body r))]
    (assoc r :head head :body body)))

(defn rename-clause-variables [r]
  (let [[m head] (rename-variables {} (:head r))
        [m2 clause] (rename-variables m (:clause r))]
    (assoc r :head head :clause clause)))

(defstruct rulebase-struct
  :table ;; map: predicate -> (seq-of rules)
  :rules ;; (seq-of rules)
  )

(defvar *empty-rulebase* (struct rulebase-struct {} '()))

(defn add-rule [rb r]
   "rulebase rule -> rulebase

   Add a rule to the rule base, for each conclusion of the rule,
   indexing it by the predicate of the conclusion.  There will be a copy
   of the rule, each with the same id, for each conclusion of the rule. This
   is an optimization, so that we don't have to iterate over the conclusions
   when trying to unify some goal with the conclusion of the rule
   with some goal."
   (reduce (fn [rb2 conclusion]
             (let [pred (predicate conclusion)
                   table (:table rb2)
                   current-rules (table pred)
                   new-rules (conj current-rules r)]
               (if (not (.contains (map :id current-rules) (:id r)))
                 (struct rulebase-struct
                         (assoc table pred new-rules)
                         (conj (:rules rb) r))
                 rb2)))
           rb
           (:head r)))

(defn add-rules [rb l]
  "rulebase (seq-of rule) -> rulebase"
  (reduce (fn [rb2 r]
            (add-rule rb2 r))
          rb
          l))

(defn rulebase [& l]
  (add-rules *empty-rulebase* l))

(let [counter (atom 0)]
  (letfn [(reset-counter []
                         (reset! counter 0))
          (get-clause-number []
                      (swap! counter inc)
                      (symbol (str "-c" @counter)))]

    (defn get-clauses [args rb goal subs]
      (let [pred (predicate (subs goal))
            applicable-rules ((:table rb) pred)
            applicable-clauses (mapinterleave (fn [rule]
                                                (reset-counter)
                                                (let [rule-clauses (:body rule)]
                                                  (if (empty? rule-clauses)
                                                    ;; force execution to keep
                                                    ;; meaningfull clause number
                                                    (list (struct named-clause
                                                                  (get-clause-number)
                                                                  (:id rule)
                                                                  (:strict rule)
                                                                  (:head rule)
                                                                  '()))
                                                    (map #(struct named-clause
                                                                  (get-clause-number)
                                                                  (:id rule)
                                                                  (:strict rule)
                                                                  (:head rule)
                                                                  %)
                                                         rule-clauses))))
                                              applicable-rules)
            applied-clauses (map symbol
                                 (schemes-applied args
                                                  (subs (statement-atom goal))))
            remaining-clauses (filter (fn [c]
                                        (not (.contains applied-clauses
                                                        (symbol
                                                         (str (:rule c)
                                                              (:id c))))))
                                      applicable-clauses)]
;        (println "--------")
;        (println "get clauses for goal:" goal)
;        (println "remaining clauses:" remaining-clauses)
;        (println "--------")
        remaining-clauses))))

(defn generate-arguments-from-rules [rb qs]
  (fn [subgoal state]
    (let [args (:arguments state)
          subs (:substitutions state),
          all-args (assert-arguments
                     args
                     (map
                       (fn [c] (instantiate-argument (:argument c) subs))
                       (:candidates state)))]

      (letfn [(apply-for-conclusion
               [clause c]
               ;; apply the clause for conclusion
               ;; in the head of the rule
               (let [subs2 (or (unify c subgoal subs)
                               (unify `(~'unless ~c)
                                      subgoal subs)
                               (unify `(~'assuming ~c)
                                      subgoal subs)
                               (unify `(~'applies ~(:rule clause) ~c) subgoal subs))]
                 (if (not subs2)
                   ;; fail
                   false
                   (let [arg-id (gensym "a")
                         direction (if (= (first subgoal) 'not) :con :pro)
                         conclusion (statement-atom (condition-statement subgoal))
                         premises (concat (map statement-to-premise (:clause clause))
                                          (rule-critical-questions (:rule clause) qs subgoal (:strict clause)))
                         scheme (str (:rule clause) (:id clause))]
                     ;(println "rule instantiated:" (str (:rule clause) (:id clause)))
                     (as/response subs2
                                  (argument arg-id
                                            false
                                            *default-weight*
                                            direction
                                            conclusion
                                            premises
                                            scheme))))))
              (apply-clause [clause]
                            (filter identity (map #(apply-for-conclusion clause %) (:head clause)) ))]
        (mapinterleave (fn [c]
                         (apply-clause c))  (map rename-clause-variables
                         (get-clauses all-args rb subgoal subs)))))))

