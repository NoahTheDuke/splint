; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.pattern2
  (:require
    [noahtheduke.splint.utils :refer [drop-quote simple-type]]))

(def specials #{:?+ :?* :??})

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
         :symbol (let [pat-name (name pattern)]
                   (case [(.charAt pat-name 0)
                          (when (< 1 (count pat-name))
                            (.charAt pat-name 1))]
                     [\_ nil] :any
                     [\? \_] :any
                     [\? \+] :?+
                     [\? \*] :?*
                     [\? \?] :??
                     [\? nil] :?
                     ; else
                     :symbol))
         :list (case (first pattern)
                 quote :quote
                 ? :?
                 ?* :?*
                 ?+ :?+
                 ?? :??
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
  (let [children-form (gensym "bind-form-")]
    `(let [~children-form ~form]
       (if-let [existing# ^clojure.lang.MapEntry (find ~ctx '~pattern)]
         (when (= (.val existing#) ~children-form)
           ~ctx)
         (assoc ~ctx '~pattern ~children-form)))))

(defn match-pred
  [ctx form pred bind]
  (let [pred-name (name pred)
        pred (or (requiring-resolve (symbol (or (namespace pred) (str *ns*)) pred-name))
                 (resolve (symbol "clojure.core" pred-name))
                 (requiring-resolve (symbol "noahtheduke.splint.rules.helpers" pred-name)))
        bind (when-not (#{'_ '?_} bind) (symbol (str "?" bind)))]
    `(let [form# ~form
           result# (~pred form#)]
       (when result#
         ~(if (some? bind)
            (match-binding ctx bind form)
            ctx)))))

(defmethod read-form :? [ctx pattern form]
  (let [pattern (if (symbol? pattern) ['? (subs (str pattern) 1)] pattern)
        [_?sym bind & [pred]] pattern]
    (cond
      (nil? pred)
      (match-binding ctx (symbol (str "?" bind)) form)
      (symbol? pred)
      (match-pred ctx form pred bind)
      :else
      (throw (ex-info "Predicate must be a symbol" {:pred pred})))))

(defn match-star
  [ctx pattern]
  (let [pattern (if (symbol? pattern) ['?* (subs (str pattern) 2)] pattern)
        [_?sym bind & [pred]] pattern
        body-form (gensym "star-form-")
        pred-check (if pred `(every? ~pred ~body-form) true)]
    `(fn [~ctx form# cont#]
       (let [max-len# (count form#)]
         (loop [i# 0
                ~body-form (vec (take i# form#))]
           (when (<= i# max-len#)
             (or (and ~pred-check
                      (let [~ctx ~(match-binding ctx (symbol (str "?" bind)) body-form)]
                        (cont# ~ctx (drop i# form#))))
                 (recur (inc i#)
                        (if (< (count ~body-form) max-len#)
                          (conj ~body-form (nth form# (count ~body-form)))
                          ~body-form)))))))))

(defn match-plus
  [ctx pattern]
  (let [pattern (if (symbol? pattern) ['?+ (subs (str pattern) 2)] pattern)
        [_?sym bind & [pred]] pattern
        body-form (gensym "plus-form-")
        pred-check (if pred `(every? ~pred ~body-form) true)]
    `(fn [~ctx form# cont#]
       (let [max-len# (count form#)]
         (loop [i# 1
                ~body-form (vec (take i# form#))]
           (when (<= i# max-len#)
             (or (and ~pred-check
                      (let [~ctx ~(match-binding ctx (symbol (str "?" bind)) body-form)]
                        (cont# ~ctx (drop i# form#))))
                 (recur (inc i#)
                        (if (< (count ~body-form) max-len#)
                          (conj ~body-form (nth form# (count ~body-form)))
                          ~body-form)))))))))

(defn match-optional
  [ctx pattern]
  (let [pattern (if (symbol? pattern) ['?? (subs (str pattern) 2)] pattern)
        [_?sym bind & [_pred]] pattern
        body-form (gensym "optional-form-")]
    `(fn [~ctx form# cont#]
       (let [ctx# ~(match-binding ctx (symbol (str "?" bind)) '())]
         (or (cont# ctx# form#)
             (when (seq form#)
               (let [~body-form (take 1 form#)
                     ~ctx ~(match-binding ctx (symbol (str "?" bind)) body-form)]
                 (cont# ~ctx (drop 1 form#)))))))))

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
    `(fn [~ctx ~children-form cont#]
       (let [~@binds]
         (when ~ctx
           (cont# ~ctx ~children-form))))))

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
  (let [pattern (mapv (juxt read-dispatch identity) pattern)
        min-length (->> pattern
                        (remove #(specials (first %)))
                        count)
        children-form (gensym "variable-seq-form-")
        fns (->> pattern
                 (partition-by #(specials (first %)))
                 (mapv
                   (fn [items]
                     (let [t (ffirst items)
                           items (mapv second items)]
                       (case t
                         :?+
                         (mapcat
                           identity
                           (for [item items]
                             [(gensym "plus-fn-") (match-plus ctx item)]))
                         :?*
                         (mapcat
                           identity
                           (for [item items]
                             [(gensym "star-fn-") (match-star ctx item)]))
                         :??
                         (mapcat
                           identity
                           (for [item items]
                             [(gensym "optional-fn-") (match-optional ctx item)]))
                         ; else
                         [(gensym "single-fn-") (match-single ctx items)]))))
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
  `(if (some #(specials (read-dispatch %)) ~pattern)
     (variable-seq-match ~ctx ~pattern ~form)
     (simple-seq-match ~ctx ~pattern ~form)))

(defmethod read-form :list [ctx pattern form]
  (seq-match ctx pattern form))

(defmethod read-form :vector [ctx pattern form]
  (seq-match ctx pattern form))

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

(comment
  (require '[pattern :refer [compile-pattern]]
           '[noahtheduke.splint.pattern :as p])
  (def p0 (p/pattern '[%true?%-?a 1 2 &&. ?b]))
  (def p1 (compile-pattern '[(? a true?) ??b 1 2 ??c]))
  (def p2 (pattern '[1 ??a 3]))
  (p2 '[1 2 3])

  #_(do (user/quick-bench (p0 '[true 1 2]))
      (flush)
      (user/quick-bench (p1 '[true 1 2]))
      (flush)
      (user/quick-bench (p2 '[true 1 2])))
  )
