; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.pattern
  (:require
    [noahtheduke.splint.utils :refer [drop-quote simple-type]]))

(set! *warn-on-reflection* true)

(def special? #{:?+ :?* :?? :?|})

(defn literal?
  "Is a given simple-type a literal/non-special?"
  [t]
  (#{:nil :boolean :char :number :keyword :string :symbol :quote :list :map :set} t))

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
         :symbol (let [pat-name (name pattern)
                       char0 (.charAt pat-name 0)]
                   (case [char0
                          (when (< 1 (count pat-name))
                            (.charAt pat-name 1))]
                     [\_ nil] :any
                     [\? \_] :any
                     [\? \+] :?+
                     [\? \*] :?*
                     [\? \?] :??
                     [\? \|] :?|
                     ; else
                     (if (= \? char0)
                       :?
                       :symbol)))
         :list (case (first pattern)
                 quote :quote
                 ? :?
                 ?* :?*
                 ?+ :?+
                 ?? :??
                 ?| :?|
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

(defmethod read-form :nil [ctx _pattern form]
  `(when (nil? ~form)
     ~ctx))

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
  "Pattern must be a symbol in ?x style already."
  [ctx bind form]
  (let [children-form (gensym "bind-form-")]
    `(if (#{'~'_ '~'?_} '~bind)
       ~ctx
       (let [~children-form ~form]
         (if-let [existing# ^clojure.lang.MapEntry (find ~ctx '~bind)]
           (when (= (val existing#) ~children-form)
             ~ctx)
           (assoc ~ctx '~bind ~children-form))))))

(defn match-pred
  [ctx bind form pred]
  (let [pred-name (name pred)
        pred (or (requiring-resolve (symbol (or (namespace pred) (str *ns*)) pred-name))
                 (resolve (symbol "clojure.core" pred-name))
                 (requiring-resolve (symbol "noahtheduke.splint.rules.helpers" pred-name)))
        children-form (gensym "pred-form-")]
    `(let [~children-form ~form]
       (when (~pred ~children-form)
         ~(match-binding ctx bind children-form)))))

(defmethod read-form :? [ctx pattern form]
  (let [pattern (if (symbol? pattern) ['? (subs (str pattern) 1)] pattern)
        [_?sym bind & [pred]] pattern]
    (cond
      (nil? pred)
      (match-binding ctx (symbol (str "?" bind)) form)
      (symbol? pred)
      (match-pred ctx (symbol (str "?" bind)) form pred)
      :else
      (throw (ex-info "Predicate must be a symbol" {:pred pred})))))

(defn match-star
  [ctx pattern]
  (let [pattern (if (symbol? pattern) ['?* (subs (str pattern) 2)] pattern)
        [_?sym bind & [pred]] pattern
        body-form (gensym "star-form-")
        pred-check (if pred `(every? ~pred ~body-form) true)]
    [(gensym "star-fn-")
     `(fn [~ctx form# cont#]
        (let [max-len# (count form#)]
          (loop [i# 0
                 ~body-form (vary-meta (vec (take i# form#)) assoc ::rest true)]
            (when (<= i# max-len#)
              (or (and ~pred-check
                       (let [~ctx ~(match-binding ctx (symbol (str "?" bind)) body-form)]
                         (cont# ~ctx (drop i# form#))))
                  (recur (inc i#)
                         (if (< (count ~body-form) max-len#)
                           (conj ~body-form (nth form# (count ~body-form)))
                           ~body-form)))))))]))

(defn match-plus
  [ctx pattern]
  (let [pattern (if (symbol? pattern) ['?+ (subs (str pattern) 2)] pattern)
        [_?sym bind & [pred]] pattern
        body-form (gensym "plus-form-")
        pred-check (if pred `(every? ~pred ~body-form) true)]
    [(gensym "plus-fn-")
     `(fn [~ctx form# cont#]
        (let [max-len# (count form#)]
          (loop [i# 1
                 ~body-form (vary-meta (vec (take i# form#)) assoc ::rest true)]
            (when (<= i# max-len#)
              (or (and ~pred-check
                       (let [~ctx ~(match-binding ctx (symbol (str "?" bind)) body-form)]
                         (cont# ~ctx (drop i# form#))))
                  (recur (inc i#)
                         (if (< (count ~body-form) max-len#)
                           (conj ~body-form (nth form# (count ~body-form)))
                           ~body-form)))))))]))

(defn match-optional
  [ctx pattern]
  (let [pattern (if (symbol? pattern) ['?? (subs (str pattern) 2)] pattern)
        [_?sym bind & [pred]] pattern
        body-form (gensym "optional-form-")
        pred-check (if pred `(every? ~pred ~body-form) true)]
    [(gensym "optional-fn-")
     `(fn [~ctx form# cont#]
        (let [~body-form (vary-meta () assoc ::rest true)]
          (or (and ~pred-check
                   (let [ctx# ~(match-binding ctx (symbol (str "?" bind)) body-form)]
                     (cont# ctx# form#)))
              (when (seq form#)
                (let [~body-form (vary-meta (vec (take 1 form#)) assoc ::rest true)]
                  (when ~pred-check
                    (let [ctx# ~(match-binding ctx (symbol (str "?" bind)) body-form)]
                      (cont# ctx# (drop 1 form#)))))))))]))

(defn match-alt
  [ctx pattern]
  (when (symbol? pattern)
    (throw (IllegalArgumentException. "Can't use ?| on a symbol")))
  (when-not (vector? (-> pattern next next first))
    (throw (IllegalArgumentException. "?| arg must be vector of alts")))
  (when-not (every? #(literal? (read-dispatch %)) (-> pattern next next first))
    (throw (IllegalArgumentException. "?| alts must be literals")))
  (let [[_?sym bind & [alts]] pattern
        temp-ctx (gensym "temp-ctx-")
        body-form (gensym "alt-body-form-")
        binds (mapcat
                (fn [alt-pattern]
                  [temp-ctx
                   `(or ~temp-ctx
                        (let [~body-form (first ~body-form)]
                          (when ~(read-form ctx alt-pattern body-form)
                            ~(match-binding ctx (symbol (str "?" bind)) body-form))))
                   body-form
                   `(if ~temp-ctx
                      (next ~body-form)
                      ~body-form)])
                alts)]
    [(gensym "alt-fn-")
     `(fn [~ctx ~body-form cont#]
        (let [~temp-ctx nil
              ~@binds
              ~ctx ~temp-ctx]
          (when ~ctx
            (cont# ~ctx ~body-form))))]))

(defn- match-single-binds
  [ctx children-form items]
  (mapcat
    (fn [item]
      [ctx `(when ~ctx
              ~(read-form ctx item `(first ~children-form)))
       children-form `(when ~ctx
                        (next ~children-form))])
    items))

(defn match-single
  "Checks multiple single-element values at once.

  Instead of generating a big `and` like in v1, this rebinds ctx and the
  children-form for each pattern, returning `nil` if one of the patterns
  doesn't match and somewhat short-circuiting the rest of the checks.

  Relies on the output of the single-element pattern functions and expects them
  to return the ctx object iff there's a match."
  [ctx items]
  (let [children-form (gensym "many-normal-form-")
        binds (match-single-binds ctx children-form items)]
    [(gensym "single-fn-")
     `(fn [~ctx ~children-form cont#]
        (let [~@binds]
          (when ~ctx
            (cont# ~ctx ~children-form))))]))

(defn seq-match-step
  "Inspired by segment-matcher from pangloss/pattern."
  [ctx form fns]
  (cond
    (seq fns) ((first fns)
               ctx
               form
               (fn [ctx new-form]
                 (seq-match-step ctx new-form (next fns))))
    (empty? form) ctx))

(defn variable-seq-match [ctx pattern form]
  (let [pattern-pairs (mapv (juxt read-dispatch identity) pattern)
        min-length (->> pattern-pairs
                        (remove #(special? (first %)))
                        count)
        children-form (gensym "variable-seq-form-")
        fns (->> pattern-pairs
                 (partition-by #(special? (first %)))
                 (mapv
                   (fn [pattern-pairs]
                     (let [t (ffirst pattern-pairs)
                           patterns (mapv second pattern-pairs)]
                       (case t
                         :?+
                         (mapcat
                           identity
                           (for [pattern patterns]
                             (match-plus ctx pattern)))
                         :?*
                         (mapcat
                           identity
                           (for [pattern patterns]
                             (match-star ctx pattern)))
                         :??
                         (mapcat
                           identity
                           (for [pattern patterns]
                             (match-optional ctx pattern)))
                         :?|
                         (mapcat
                           identity
                           (for [pattern patterns]
                             (match-alt ctx pattern)))
                         ; else
                         (match-single ctx patterns)))))
                 (mapcat identity))
        fn-names (take-nth 2 fns)
        type? (if (list? pattern) `list? `vector?)]
    `(let [~children-form ~form]
       (and (~type? ~children-form)
            (<= ~min-length (count ~children-form))
            (let [~@fns]
              (seq-match-step ~ctx ~children-form ~(vec fn-names)))))))

(defn simple-seq-match [ctx pattern form]
  (let [children-form (gensym "simple-seq-form-")
        binds (match-single-binds ctx children-form pattern)
        type? (if (list? pattern) `list? `vector?)]
    `(let [~children-form ~form]
       (and (~type? ~children-form)
            (= ~(count pattern) (count ~children-form))
            (let [~@binds]
              ~ctx)))))

(defmacro seq-match [ctx pattern form]
  `(if (some #(special? (read-dispatch %)) ~pattern)
     (variable-seq-match ~ctx ~pattern ~form)
     (simple-seq-match ~ctx ~pattern ~form)))

(defmethod read-form :list [ctx pattern form]
  (seq-match ctx pattern form))

(defmethod read-form :vector [ctx pattern form]
  (seq-match ctx pattern form))

(defn non-coll?
  "Is a given simple-type a non-collection?"
  [t]
  (case t
    (:nil :boolean :char :number :keyword :string :symbol) true
    false))

(defmethod read-form :map [ctx pattern form]
  {:pre [(every? (comp non-coll? simple-type) (keys pattern))]}
  (let [new-form (gensym "map-form-")
        binds (mapcat
                (fn [[k v]]
                  [ctx
                   `(when (contains? ~new-form ~k)
                      ~(read-form ctx v `(~new-form ~k)))])
                pattern)]
    `(let [~new-form ~form]
       (and (map? ~new-form)
            (<= ~(count (keys pattern)) (count ~new-form))
            (let [~@binds]
              ~ctx)))))

(defmethod read-form :set [ctx _pattern _form]
  `(when (set? ~ctx)
     ~ctx))

; (defn vec-remove
;   "remove elem in coll
;   from: https://stackoverflow.com/a/18319708/3023252"
;   [pos coll]
;   (vec (concat (subvec coll 0 pos) (subvec coll (inc pos)))))

;; TODO: implement this with ctx-passing style
#_(defmethod read-form :set [sexp form retval]
  (let [new-form (gensym "set-new-form-")
        [simple-vals complex-vals] (reduce (fn [acc cur]
                                             (if (non-coll? (simple-type cur))
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
                                  :when (not (contains? ~(set simple-vals) child#))]
                              child#))]
                  (or (empty? complex-keys-preds#)
                      (when-let [cur-pred# (first complex-keys-preds#)]
                        (when-let [idx#
                                   (loop [idx# 0]
                                     (when-let [cur-child# (nth complex-children# idx# nil)]
                                       (if (cur-pred# cur-child#)
                                         idx#
                                         (recur (inc idx#)))))]

                          (recur (next complex-keys-preds#)
                                 (vec-remove idx# complex-children#)))))))))))

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
