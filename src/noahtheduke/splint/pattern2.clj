(ns noahtheduke.splint.pattern2 
  (:require
    [noahtheduke.splint.utils :refer [drop-quote simple-type]]
    [clojure.string :as str]))

(defn read-dispatch
  "Same as [[simple-type]] except that :symbol and :list provide hints about
  their contents. Can be skipped by adding the metadata `:splint/lit`."
  ([pattern] (read-dispatch nil pattern nil))
  ([_ctx pattern _form]
   (let [type (simple-type pattern)
         smeta (meta pattern)]
     (if (and smeta (smeta :splint/lit))
       type
       (case type
         :symbol (if (#{'_ '?_} pattern)
                   :any
                   (let [pat-name (name pattern)]
                     (cond
                       (str/starts-with? pat-name "??") :bind-many
                       (str/starts-with? pat-name "?") :bind-one
                       :else :symbol)))
         :list (case (first pattern)
                 quote :quote
                 ? :bind-one
                 ; ??
                 ; else
                 :list)
         ; else
         type)))))

(defmulti read-form
  "Implementation of the main logic of [[pattern]]. Requires form and ctx
  symbols to be provided to allow for recursion."
  {:arglists '([ctx pattern form])}
  #'read-dispatch)

(remove-all-methods read-form)

(defmethod read-form :default [ctx pattern form]
  `(throw (ex-info "default" {:type ~(read-dispatch ctx pattern form)
                              :pattern '~pattern})))

(defmethod read-form :any [ctx _pattern _form]
  ctx)

(defmethod read-form :boolean [ctx pattern form]
  `(when (identical? ~pattern ~form)
     ~ctx))

(defmethod read-form :char [ctx pattern form]
  `(when (identical? ~pattern ~form)
     ~ctx))

(defmethod read-form :number [ctx pattern form]
  `(when (or (identical? ~pattern ~form) (= ~pattern ~form))
     ~ctx))

(defmethod read-form :keyword [ctx pattern form]
  `(when (identical? ~pattern ~form)
     ~ctx))

(defmethod read-form :string [ctx pattern form]
  `(when (.equals ^String ~pattern ~form)
     ~ctx))

(defmethod read-form :symbol [ctx pattern form]
  `(when (= '~pattern ~form)
     ~ctx))

(defmethod read-form :quote [ctx pattern form]
  (let [children-form (gensym "quote-form-")
        interior-pattern (second pattern)
        interior-pattern (if (instance? clojure.lang.IObj interior-pattern)
                           (vary-meta interior-pattern assoc :splint/lit true)
                           interior-pattern)]
    `(let [~children-form ~form]
       (and (list? ~children-form)
            (= 'quote (first ~children-form))
            (let [~children-form (second ~children-form)]
              ~(read-form ctx interior-pattern children-form))))))

(defn match-binding
  "Pattern must be a symbol in ?x form already."
  [ctx pattern form]
  `(if-let [existing# ^clojure.lang.MapEntry (find ~ctx '~pattern)]
     (when (= (.val existing#) ~form)
       ~ctx)
     (assoc ~ctx '~pattern ~form)))

(defn match-pred
  [ctx form pred bind]
  (let [pred-name (name pred)
        pred (or (requiring-resolve (symbol (or (namespace pred) (str *ns*)) pred-name))
                 (resolve (symbol "clojure.core" pred-name))
                 (requiring-resolve (symbol "noahtheduke.splint.rules.helpers" pred-name)))
        bind (when-not (#{'_ '?_} bind) (symbol (str "?" bind)))]
    `(let [form# ~form
           result# (~(deref pred) form#)]
       (when result#
         ~(if (some? bind)
            (match-binding ctx bind form)
            ctx)))))

(defmethod read-form :bind-one [ctx pattern form]
  (let [pattern (if (symbol? pattern) ['? (subs (str pattern) 1)] pattern)
        [_?sym bind & [pred]] pattern]
    (cond
      (nil? pred)
      (match-binding ctx (symbol (str "?" bind)) form)
      (symbol? pred)
      (match-pred ctx form pred bind)
      :else
      (throw (ex-info "Predicate must be a symbol" {:pred pred})))))

(defmethod read-form :list [ctx pattern form]
  (let [types (mapv read-dispatch pattern)
        min-length (count (remove #{:bind-many} types))
        variable-length? (some #{:bind-many} types)]
    (if variable-length?
      ctx
      (let [children-form (gensym "list-form-")
            preds (keep-indexed
                    (fn [idx item]
                      (let [n (repeat idx `next)]
                        (read-form ctx item `(-> ~@n first ~children-form))))
                    pattern)]
        `(let [~children-form ~form]
           (and (list? ~children-form)
                (= ~min-length (count ~children-form))
                ~@preds))))))

(defmacro pattern
  "Parse a provided pattern s-expression into a function that checks each
  element and sub-element of the form as a whole predicate. Makes semi-smart
  decisions about using let-bindings to avoid re-accessing the same value
  multiple times, adding type hints to rely on interop, and handles the
  complexities of the pattern DLS.

  Returns a map or `nil`. If the provided pattern uses bindings, the map will
  have the bindings as keys."
  [pattern]
  (let [form (gensym "form-")
        ctx (gensym "ctx-")]
    `(fn [~form]
       (let [~ctx {}]
         (or ~(read-form ctx (drop-quote pattern) form) nil)))))

(let [pat (pattern '(a ?b ??a))]
  (pat :a))
