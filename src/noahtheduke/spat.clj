; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.spat
  (:refer-clojure :exclude [run!])
  (:require
   [clojure.java.io :as io]
   [clojure.pprint :as pprint]
   [clojure.string :as str]
   [clojure.walk :as walk]
   [edamame.core :as e])
  (:import
   (clojure.lang PersistentList)
   (java.io File)))

(set! *warn-on-reflection* true)

(def clj-defaults
  {:all true
   :quote true
   :row-key :line
   :col-key :column
   :end-location false
   :location? seq?
   :features #{:cljs}
   :read-cond :preserve
   :auto-resolve (fn [x] (if (= :current x) *ns* (get (ns-aliases *ns*) x)))
   :readers {'js (fn [v] (list 'js v))}})

(defn parse-string [s] (e/parse-string s clj-defaults))
(defn parse-string-all [s] (e/parse-string-all s clj-defaults))

(defn ->list
  "Alternative to (apply list (...)) to get concrete list"
  [coll]
  (PersistentList/create coll))

(defn drop-quote
  "(quote (a b c)) -> (a b c)"
  [sexp]
  (if (and (seq? sexp)
           (= 'quote (first sexp)))
    (fnext sexp)
    sexp))

(defn simple-type [sexp]
  (cond
    ; literals
    (nil? sexp) :nil
    (boolean? sexp) :boolean
    (char? sexp) :char
    (number? sexp) :number
    (keyword? sexp) :keyword
    (string? sexp) :string
    (symbol? sexp) :symbol
    ; reader macros
    (map? sexp) :map
    (set? sexp) :set
    (vector? sexp) :vector
    (seq? sexp) :list
    :else (type sexp)))

(comment
  (simple-type {:a 1})
  (simple-type (Object.)))

(defn read-dispatch [sexp _form _retval]
  (let [type (simple-type sexp)]
    (case type
      :symbol (let [s-name (name sexp)]
                (cond
                  (= '_ sexp) :any
                  (= \% (first s-name)) :pred
                  (= \? (first s-name)) :var
                  (= "&&." (subs s-name 0 (min (count s-name) 3))) :rest
                  :else :symbol))
      :list (if (= 'quote (first sexp))
              :quote
              :list)
      ;; else
      type)))

(defmulti read-form #'read-dispatch)

(defmacro pattern
  [sexp]
  (let [form (gensym "form-")
        retval (gensym "retval-")
        sexp (drop-quote sexp)]
    `(fn [~form]
       (let [~retval (atom {})]
         (when ~(read-form sexp form retval)
           @~retval)))))

(comment
  ((pattern '(loop [] (when ?test &&. ?exprs ?foo (recur))))
   (parse-string "(loop [] (when (= 1 1) (prn 1) (prn 2) (foo bar) (recur)))"))
  ((pattern '(cond &&. ?pairs %not-else ?else))
    (parse-string "`(cond (pos? x) (inc x) :default -1)"))
  ,)

(defmethod read-form :default [sexp form retval]
  `(do (throw (ex-info "default" {:type (read-dispatch ~sexp ~form ~retval)}))
       false))

(defmethod read-form :nil [_sexp form _retval]
  `(nil? ~form))

(defmethod read-form :boolean [sexp form _retval]
  `(identical? ~sexp ~form))

(defmethod read-form :char [sexp form _retval]
  `(identical? ~sexp ~form))

(defmethod read-form :number [sexp form _retval]
  `(or (identical? ~sexp ~form) (= ~sexp ~form)))

(defmethod read-form :keyword [sexp form _retval]
  `(identical? ~sexp ~form))

(defmethod read-form :string [sexp form _retval]
  `(.equals ^String ~sexp ~form))

(defn get-simple-val [form]
  (or (:value form)
      (:k form)
      (:sym form)
      (some->> (:lines form) (str/join "\n"))))

(defn get-val [form]
  (or (get-simple-val form)
      (:children form)))

(defmethod read-form :any [_sexp _form _revtal] nil)

(defmethod read-form :pred [sexp form retval]
  ;; pred has to be unquoted and then quoted to keep it a simple keyword,
  ;; not fully qualified, as it needs to be resolved in the calling namespace
  ;; to allow for custom predicate functions
  (let [[pred bind] (str/split (name sexp) #"%-")
        pred (subs pred 1)
        pred (or (resolve (symbol "clojure.core" pred))
                 (requiring-resolve (symbol (or (namespace (symbol pred)) (str *ns*)) pred)))
        bind (when bind (symbol bind))]
    `(let [form# ~form
           result# (~pred form#)]
       (when (and result# ~(some? bind))
         (swap! ~retval assoc '~bind form#))
       result#)))

(defmethod read-form :var [sexp form retval]
  `(if-let [existing# (get @~retval '~sexp)]
     (= existing# ~form)
     (do (swap! ~retval assoc '~sexp ~form)
         true)))

(defmethod read-form :symbol [sexp form _retval]
  `(= '~sexp ~form))

(defn- accrue-preds
  [sexp children-form retval]
  (keep-indexed
    (fn [idx item]
      (read-form item `(nth ~children-form ~idx) retval))
    sexp))

(defn- accrue-preds-backward
  [sexp children-form retval]
  (keep-indexed
    (fn [idx item]
      (read-form item `(nth ~children-form (- (count ~children-form) ~(inc idx))) retval))
    (reverse sexp)))

(defn- rest-form [sym form retval]
  `(if-let [existing# (get @~retval '~sym)]
     (= existing# (->list ~form))
     (do (swap! ~retval assoc '~sym (->list ~form))
         true)))

(defn- build-rest-pred [rest-sexp start end children-form retval]
  (let [[_&& rest-sym] rest-sexp]
    (assert rest-sym "&&. needs a follow-up sym")
    (rest-form
      rest-sym
      `(take (- (count ~children-form) ~(+ start end)) (drop ~start ~children-form))
      retval)))

(defmethod read-form :quote [sexp form retval]
  (let [sexp (if (seq? sexp) sexp [sexp])
        children-form (gensym "quote-form-")
        preds (accrue-preds sexp children-form retval)]
    `(let [~children-form ~form]
       (and (= 2 (count ~children-form))
                   ~@preds))))

(defn- read-form-seq [sexp form retval f]
  (let [children-form (gensym (str (name f) "-form-"))
        [front-sexp rest-sexp] (split-with #(not= '&&. %) sexp)
        preds (accrue-preds front-sexp children-form retval)
        post-rest-preds (when (seq rest-sexp)
                          (accrue-preds-backward (drop 2 rest-sexp) children-form retval))
        rest-pred (when (seq rest-sexp)
                    (build-rest-pred (take 2 rest-sexp)
                                     (count front-sexp)
                                     (count post-rest-preds)
                                     children-form
                                     retval))
        preds (filterv some? (concat preds [rest-pred] post-rest-preds))
        ;; If there's a rest arg, then count of given will be less than or equal
        size-pred (if rest-pred
                    `(<= ~(- (count sexp) 2) (count ~children-form))
                    `(= ~(count sexp) (count ~children-form)))]
    `(let [~children-form ~form]
       (and (~(resolve f) ~children-form)
            ~size-pred
            ~@preds))))

(defmethod read-form :list [sexp form retval]
  (read-form-seq sexp form retval 'list?))

(defmethod read-form :vector [sexp form retval]
  (read-form-seq sexp form retval 'vector?))

(def simple? #{:nil :boolean :char :number :keyword :string :symbol})

(defmethod read-form :map [sexp form retval]
  {:pre [(every? (comp simple? simple-type) (keys sexp))]}
  (let [new-form (gensym "map-form-")
        simple-keys (filterv #(simple? (simple-type %)) (keys sexp))
        simple-keys-preds (->> (select-keys sexp simple-keys)
                               (mapcat (fn [[k v]]
                                         [`(contains? ~new-form ~k)
                                          (read-form v `(~new-form ~k) retval)])))]
    `(let [~new-form ~form]
       (and (map? ~new-form)
            (= ~(count simple-keys) (count ~new-form))
            ~@simple-keys-preds))))

(defn vec-remove
  "remove elem in coll
  from: https://stackoverflow.com/a/18319708/3023252"
  [pos coll]
  (vec (concat (subvec coll 0 pos) (subvec coll (inc pos)))))

(defmethod read-form :set [sexp form retval]
  (let [new-form (gensym "set-new-form-")
        [simple-vals complex-vals] (reduce (fn [acc cur]
                                             (if (simple? (simple-type cur))
                                              (update acc 0 conj cur)
                                              (update acc 1 conj cur)))
                                           [[] []]
                                           sexp)
        simple-keys-preds (map (fn [k] `(contains? ~new-form ~k)) simple-vals)
        current-child (gensym "set-current-child-")
        complex-keys-preds (mapv (fn [k]
                                   `(fn [~current-child]
                                      ~(read-form k current-child retval)))
                                complex-vals)
        ]
    `(when-let [~new-form ~form]
       (and (set? ~new-form)
            (<= ~(count sexp) (count ~new-form))
            (or ~(empty? simple-keys-preds)
                (and ~@simple-keys-preds))
            (or ~(empty? complex-keys-preds)
                ;; loop over both the predicates and the children.
                ;; for each predicate, compare it against each child
                ;; until it finds a match, and then remove the child
                ;; from the list of children and recur.
                (loop [complex-keys-preds# (seq ~complex-keys-preds)
                       complex-children#
                       (vec (for [child# ~new-form
                                  :when (not (contains? ~new-form child#))]
                              child#))]
                  (or (empty? complex-keys-preds#)
                      (when-let [cur-pred# (first complex-keys-preds#)]
                        (let [idx#
                              (loop [idx# 0]
                                (when-let [cur-child# (nth complex-children# idx# nil)]
                                  (if (cur-pred# cur-child#)
                                    idx#
                                    (recur (inc idx#)))))]
                          (when idx#
                            (recur (next complex-keys-preds#) (vec-remove idx# complex-children#))))))))))))

(defn postwalk-splicing-replace [smap replace-form]
  (walk/postwalk
    (fn [item]
      (cond
        (list? item)
        (let [[front-sexp rest-sexp] (split-with #(not= '&&. %) item)]
          (->> (concat (second rest-sexp) (drop 2 rest-sexp))
               (concat front-sexp)
               (->list)))
        (contains? smap item) (smap item)
        :else
        item))
    replace-form))

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
                   (->list `(~(first ?form) ~?arg ~@(rest ?form)))
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
                   (->list (concat ?form [?arg]))
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
                 (->list `(~(symbol (str "." ?method)) ~?obj ~@?args)))})

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
                 (->list `(~(symbol (str ?class "/" ?method)) ~@?args)))})

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

(defn check-rule [rule form]
  (let [pattern (:pattern rule)
        replace (or (:replace rule) (:replace-fn rule))]
    (when-let [result (pattern form)]
      (if replace
        (replace result)
        result))))

(defn check-multiple-rules [rules form]
  (reduce
    (fn [_ rule]
      (when-let [alt (try (check-rule rule form)
                          (catch Throwable e
                            (throw (ex-info (ex-message e)
                                          (merge {:rule-name (:name rule)
                                                  :form form
                                                  :line (:line (meta form))
                                                  :column (:column (meta form))}
                                                 (ex-data e))
                                          e))))]
        (let [form-meta (meta form)]
          (reduced {:rule-name (:name rule)
                    :form form
                    :line (:line form-meta)
                    :column (:column form-meta)
                    :alt alt}))))
    nil
    rules))

(defn check-all-rules [form]
  (check-multiple-rules all-rules form))

(defn run!
  [proc coll]
  (reduce (fn [_ cur] (proc cur) nil) nil coll)
  nil)

(defn check-subforms [filename form progress]
  (let [alt-map (try (check-all-rules form)
                     (catch clojure.lang.ExceptionInfo e
                       (throw (ex-info (ex-message e)
                                       (assoc (ex-data e) :filename filename)
                                       e))))]
    (when alt-map
      (swap! progress conj (assoc alt-map :filename filename)))
    (when (seqable? form)
      (run! #(check-subforms filename % progress) form))))

(defn check-all-forms [^File file progress]
  (let [parsed-file (parse-string-all (slurp file))
        filename (str file)]
    (run! #(check-subforms filename % progress) parsed-file)))

(defn print-find [{:keys [filename rule-name form line column alt]}]
  (printf "[:%s] %s - %s:%s" rule-name filename line column)
  (newline)
  (pprint/pprint form)
  (println "Consider using:")
  (pprint/pprint alt)
  (newline)
  (flush))

(defn -main [& args]
  (let [[dir] args
        progress (atom [])
        start-time (System/nanoTime)
        files (->> (io/file dir)
                   (file-seq)
                   (filter #(and (.isFile ^File %)
                                 (some (fn [ft] (str/ends-with? % ft)) [".clj" ".cljs" ".cljc"])))
                   (sort))
        _ (->> files
               (pmap #(check-all-forms % progress))
               doall)
        end-time (System/nanoTime)]
    (doseq [violation (sort-by :filename @progress)]
      (print-find violation))
    (printf "Linting took %sms, %s style warnings%n"
            (int (/ (double (- end-time start-time)) 1000000.0))
            (count @progress))
    (flush)
    (System/exit (count @progress))))
