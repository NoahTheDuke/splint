; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.spat.rules
  (:require
   [clojure.walk :as walk]
   [noahtheduke.spat.pattern :refer [pattern simple-type]]))

(defn postwalk-splicing-replace [smap replace-form]
  (walk/postwalk
    (fn [item]
      (cond
        (seq? item)
        (let [[front-sexp rest-sexp] (split-with #(not= '&&. %) item)]
          (concat front-sexp (second rest-sexp) (drop 2 rest-sexp)))
        (contains? smap item) (smap item)
        :else
        item))
    replace-form))

(defn check-rule [rule form]
  (let [pattern (:pattern rule)
        replace (or (:replace rule) (:replace-fn rule))]
    (when-let [result (pattern form)]
      (if replace
        (replace result)
        result))))

(defmacro defrule
  [rule-name & opts]
  (let [docs (when (string? (first opts)) [(first opts)])
        opts (if (string? (first opts)) (next opts) opts)
        {:keys [pattern replace replace-fn]} opts]
    (assert (simple-symbol? rule-name) "defrule name cannot be namespaced")
    (assert (not (and replace replace-fn))
            "defrule cannot define both replace and replace-fn")
    `(def ~rule-name
       ~@docs
       {:name ~(str rule-name)
        :docstring ~(first docs)
        :init-type (simple-type ~pattern)
        :pattern-raw ~pattern
        :replace-raw ~replace
        :pattern (pattern ~pattern)
        :replace ~(when replace
                    `(fn ~(symbol (str rule-name "-replacer-fn"))
                       [smap#]
                       (postwalk-splicing-replace smap# ~replace)))
        :replace-fn ~replace-fn})))

(defrule plus-x-1
  {:pattern '(+ ?x 1)
   :replace '(inc ?x)})

(defrule plus-1-x
  {:pattern '(+ 1 ?x)
   :replace '(inc ?x)})

(defrule minus-x-1
  {:pattern '(- ?x 1)
   :replace '(dec ?x)})

(defrule nested-muliply
  {:pattern '(* ?x (* &&. ?xs))
   :replace '(* ?x &&. ?xs)})

(defrule nested-addition
  {:pattern '(+ ?x (+ &&. ?xs))
   :replace '(+ ?x &&. ?xs)})

(defrule plus-0
  {:pattern '(+ ?x 0)
   :replace '?x})

(defrule minus-0
  {:pattern '(- ?x 0)
   :replace '?x})

(defrule multiply-by-1
  {:pattern '(* ?x 1)
   :replace '?x})

(defrule divide-by-1
  {:pattern '(/ ?x 1)
   :replace '?x})

(defrule multiply-by-0
  {:pattern '(* ?x 0)
   :replace '0})

(def math-rules
  [plus-x-1
   plus-1-x
   minus-x-1
   nested-muliply
   nested-addition
   plus-0
   minus-0
   multiply-by-1
   divide-by-1
   multiply-by-0])

(defrule str-to-string
  "(.toString) to (str)"
  {:pattern '(.toString ?x)
   :replace '(str ?x)})

(defrule str-apply-interpose
  "(apply str (interpose)) to (str/join)"
  {:pattern '(apply str (interpose ?x ?y))
   :replace '(clojure.string/join ?x ?y)})

(defrule str-apply-reverse
  "(apply str (reverse)) to (str/reverse)"
  {:pattern '(apply str (reverse ?x))
   :replace '(clojure.string/reverse ?x)})

(defrule str-apply-str
  "(apply str) to (str/join)"
  {:pattern '(apply str ?x)
   :replace '(clojure.string/join ?x)}) 

(def string-rules
  "All str and clojure.string related rules"
  [str-to-string
   str-apply-interpose
   str-apply-reverse
   str-apply-str])

(defrule mapcat-apply-apply
  {:pattern '(apply concat (apply map ?x ?y))
   :replace '(mapcat ?x ?y)})

(defrule mapcat-concat-map
  {:pattern '(apply concat (map ?x &&. ?y))
   :replace '(mapcat ?x &&. ?y)})

(defrule filter-complement
  {:pattern '(filter (complement ?pred) ?coll)
   :replace '(remove ?pred ?coll)})

(defrule filter-seq
  {:pattern '(filter seq ?coll)
   :replace '(remove empty? ?coll)})

(defrule filter-fn*-not-pred
  {:pattern '(filter (fn* [?x] (not (?pred ?x))) ?coll)
   :replace '(remove ?pred ?coll)})

(defrule filter-fn-not-pred
  {:pattern '(filter (fn [?x] (not (?pred ?x))) ?coll)
   :replace '(remove ?pred ?coll)})

(defrule filter-vec-filter
  {:pattern '(vec (filter ?pred ?coll))
   :replace '(filterv ?pred ?coll)})

(def sequence-rules
  [mapcat-apply-apply
   mapcat-concat-map
   filter-complement
   filter-seq
   filter-fn*-not-pred
   filter-fn-not-pred
   filter-vec-filter])

(defrule first-first
  {:pattern '(first (first ?coll))
   :replace '(ffirst ?coll)})

(defrule first-next
  {:pattern '(first (next ?coll))
   :replace '(fnext ?coll)})

(defrule next-next
  {:pattern '(next (next ?coll))
   :replace '(nnext ?coll)})

(defrule next-first
  {:pattern '(next (first ?coll))
   :replace '(nfirst ?coll)})

(def first-next-rules
  [first-first
   first-next
   next-first
   next-next])

(defrule fn*-wrapper
  {:pattern '(fn* [?arg] (?fun ?arg))
   :replace '?fun})

(defrule fn-wrapper
  {:pattern '(fn [?arg] (?fun ?arg))
   :replace '?fun})

(def fn-rules
  [fn*-wrapper
   fn-wrapper])

(defrule thread-first-no-arg
  "(-> x) to x"
  {:pattern '(-> ?x)
   :replace '?x})

(defn symbol-or-keyword-or-list? [sexp]
  (or (symbol? sexp)
      (keyword? sexp)
      (list? sexp)
      (and (sequential? sexp) (not (vector? sexp)))))

(defrule thread-first-1-arg
  "(-> x y) to (y x)
  (-> x (y)) to (y x)"
  {:pattern '(-> ?arg %symbol-or-keyword-or-list?%-?form)
   :replace-fn (fn [{:syms [?form ?arg]}]
                 (if (list? ?form)
                   `(~(first ?form) ~?arg ~@(rest ?form))
                   (list ?form ?arg)))})

(defrule thread-last-no-arg
  "(->> x) to x"
  {:pattern '(->> ?x)
   :replace '?x})

(defrule thread-last-1-arg
  "(->> x y) to (y x)
  (->> x (y)) to (y x)"
  {:pattern '(->> ?arg %symbol-or-keyword-or-list?%-?form)
   :replace-fn (fn [{:syms [?form ?arg]}]
                 (if (list? ?form)
                   (concat ?form [?arg])
                   (list ?form ?arg)))})

(def threading-rules
  [thread-first-no-arg
   thread-first-1-arg
   thread-last-no-arg
   thread-last-1-arg])

(defrule not-some-pred
  {:pattern '(not (some ?pred ?coll))
   :replace '(not-any? ?pred ?coll)})

(defrule with-meta-f-meta
  {:pattern '(with-meta ?x (?f (meta ?x) &&. ?args))
   :replace '(vary-meta ?x ?f &&. ?args)})

(defn symbol-not-class? [sym]
  (and (symbol? sym)
       (let [sym (pr-str sym)
             idx (.lastIndexOf sym ".")]
         (not (if (neg? idx)
                (Character/isUpperCase ^char (first sym))
                (Character/isUpperCase ^char (nth sym (inc idx))))))))

(defrule dot-obj-usage
  "(. obj method args) -> (.method obj args)"
  {:pattern '(. %symbol-not-class?%-?obj %symbol?%-?method &&. ?args)
   :replace-fn (fn [{:syms [?obj ?method ?args]}]
                 `(~(symbol (str "." ?method)) ~?obj ~@?args))})

(defn symbol-class? [sym]
  (and (symbol? sym)
       (let [sym (pr-str sym)
             idx (.lastIndexOf sym ".")]
         (if (neg? idx)
           (Character/isUpperCase ^char (first sym))
           (Character/isUpperCase ^char (nth sym (inc idx)))))))

(defrule dot-class-usage
  "(. Obj method args) -> (.method obj args)"
  {:pattern '(. %symbol-class?%-?class %symbol?%-?method &&. ?args)
   :replace-fn (fn [{:syms [?class ?method ?args]}]
                 `(~(symbol (str ?class "/" ?method)) ~@?args))})

(def misc-rules
  [not-some-pred
   with-meta-f-meta
   dot-obj-usage
   dot-class-usage])

;;vector
(defrule conj-vec
  {:pattern '(conj [] &&. ?x)
   :replace '(vector &&. ?x)})

(defrule into-vec
  {:pattern '(into [] ?coll)
   :replace '(vec ?coll)})

(defrule assoc-assoc-key-coll
  {:pattern '(assoc ?coll ?key0 (assoc (?key0 ?coll) ?key1 ?val))
   :replace '(assoc-in ?coll [?key0 ?key1] ?val)})

(defrule assoc-assoc-coll-key
  {:pattern '(assoc ?coll ?key0 (assoc (?coll ?key0) ?key1 ?val))
   :replace '(assoc-in ?coll [?key0 ?key1] ?val)})

(defrule assoc-assoc-get
  {:pattern '(assoc ?coll ?key0 (assoc (get ?coll ?key0) ?key1 ?val))
   :replace '(assoc-in ?coll [?key0 ?key1] ?val)})

(defrule assoc-fn-key-coll
  {:pattern '(assoc ?coll ?key (?fn (?key ?coll) &&. ?args))
   :replace '(update-in ?coll [?key] ?fn &&. ?args)})

(defrule assoc-fn-coll-key
  {:pattern '(assoc ?coll ?key (?fn (?coll ?key) &&. ?args))
   :replace '(update-in ?coll [?key] ?fn &&. ?args)})

(defrule assoc-fn-get
  {:pattern '(assoc ?coll ?key (?fn (get ?coll ?key) &&. ?args))
   :replace '(update-in ?coll [?key] ?fn &&. ?args)})

(defrule update-in-assoc
  {:pattern '(update-in ?coll ?keys assoc ?val)
   :replace '(assoc-in ?coll ?keys ?val)})

;; empty?
(defrule not-empty?
  {:pattern '(not (empty? ?x))
   :replace '(not-empty ?x)})

(defrule when-not-empty?
  {:pattern '(when-not (empty? ?x) &&. ?y)
   :replace '(when (seq ?x) &&. ?y)})

;; set
(defrule into-set
  {:pattern '(into #{} ?coll)
   :replace '(set ?coll)})

(defrule take-repeatedly
  {:pattern '(take ?n (repeatedly ?coll))
   :replace '(repeatedly ?n ?coll)})

(defrule dorun-map
  {:pattern '(dorun (map ?fn ?coll))
   :replace '(run! ?fn ?coll)})

(def coll-rules
  [conj-vec
   into-vec
   assoc-assoc-key-coll
   assoc-assoc-coll-key
   assoc-assoc-get
   assoc-fn-key-coll
   assoc-fn-coll-key
   assoc-fn-get
   update-in-assoc
   not-empty?
   when-not-empty?
   into-set
   take-repeatedly
   dorun-map])

(defrule if-else-nil
  {:pattern '(if ?x ?y nil)
   :replace '(when ?x ?y)})

(defrule if-nil-else
  {:pattern '(if ?x nil ?y)
   :replace '(when-not ?x ?y)})

(defrule if-then-do
  {:pattern '(if ?x (do &&. ?y))
   :replace '(when ?x &&. ?y)})

(defrule if-not-x-y-x
  {:pattern '(if (not ?x) ?y ?z)
   :replace '(if-not ?x ?y ?z)})

(defrule if-x-x-y
  {:pattern '(if ?x ?x ?y)
   :replace '(or ?x ?y)})

(defrule when-not-x-y
  {:pattern '(when (not ?x) &&. ?y)
   :replace '(when-not ?x &&. ?y)})

(defrule useless-do-x
  {:pattern '(do ?x)
   :replace '?x})

(defrule if-let-else-nil
  {:pattern '(if-let ?binding ?expr nil)
   :replace '(when-let ?binding ?expr)})

(defrule when-do
  {:pattern '(when ?x (do &&. ?y))
   :replace '(when ?x &&. ?y)})

(defrule when-not-do
  {:pattern '(when-not ?x (do &&. ?y))
   :replace '(when-not ?x &&. ?y)})

(defrule if-not-do
  {:pattern '(if-not ?x (do &&. ?y))
   :replace '(when-not ?x &&. ?y)})

(defrule if-not-not
  {:pattern '(if-not (not ?x) ?y ?z)
   :replace '(if ?x ?y ?z)})

(defrule when-not-not
  {:pattern '(when-not (not ?x) &&. ?y)
   :replace '(when ?x &&. ?y)})

(defrule loop-empty-when
  {:pattern '(loop [] (when ?test &&. ?exprs (recur)))
   :replace '(while ?test &&. ?exprs)})

(defrule let-do
  {:pattern '(let ?binding (do &&. ?exprs))
   :replace '(let ?binding &&. ?exprs)})

(defrule loop-do
  {:pattern '(loop ?binding (do &&. ?exprs))
   :replace '(loop ?binding &&. ?exprs)})

(defn not-else [s] (and (not= :else s)
                        (or (keyword? s)
                            (true? s))))

(defrule cond-else
  {:pattern '(cond &&. ?pairs %not-else ?else)
   :replace '(cond &&. ?pairs :else ?else)})

(def control-flow-rules
  [if-else-nil
   if-nil-else
   if-then-do
   if-not-x-y-x
   if-x-x-y
   when-not-x-y
   useless-do-x
   if-let-else-nil
   when-do
   when-not-do
   if-not-do
   if-not-not
   when-not-not
   loop-empty-when
   let-do
   loop-do
   cond-else])

(defrule not-eq
  {:pattern '(not (= &&. ?args))
   :replace '(not= &&. ?args)})

(defrule eq-0-x
  {:pattern '(= 0 ?x)
   :replace '(zero? ?x)})

(defrule eq-x-0
  {:pattern '(= ?x 0)
   :replace '(zero? ?x)})

(defrule eqeq-0-x
  {:pattern '(== 0 ?x)
   :replace '(zero? ?x)})

(defrule eqeq-x-0
  {:pattern '(== ?x 0)
   :replace '(zero? ?x)})

(defrule lt-0-x
  {:pattern '(< 0 ?x)
   :replace '(pos? ?x)})

(defrule gt-x-0
  {:pattern '(> ?x 0)
   :replace '(pos? ?x)})

(defrule lt-x-0
  {:pattern '(< ?x 0)
   :replace '(neg? ?x)})

(defrule gt-0-x
  {:pattern '(> 0 ?x)
   :replace '(neg? ?x)})

(defrule eq-true
  {:pattern '(= true ?x)
   :replace '(true? ?x)})

(defrule eq-false
  {:pattern '(= false ?x)
   :replace '(false? ?x)})

(defrule eq-x-nil
  {:pattern '(= ?x nil)
   :replace '(nil? ?x)})

(defrule eq-nil-x
  {:pattern '(= nil ?x)
   :replace '(nil? ?x)})

(defrule not-nil?
  {:pattern '(not (nil? ?x))
   :replace '(some? ?x)})

(def equality-rules
  [not-eq
   eq-0-x
   eq-x-0
   eqeq-0-x
   eqeq-x-0
   lt-0-x
   lt-x-0
   gt-0-x
   gt-x-0
   eq-true
   eq-false
   eq-x-nil
   eq-nil-x
   not-nil?])

(def all-rules
  (vec (concat string-rules
               sequence-rules
               first-next-rules
               fn-rules
               threading-rules
               misc-rules
               math-rules
               coll-rules
               control-flow-rules
               equality-rules)))

(def grouped-rules
  (group-by :init-type all-rules))

