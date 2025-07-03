; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.pattern
  (:require
   [clojure.string :as str]
   [noahtheduke.splint.clojure-ext.core :refer [postwalk* vary-meta*]]
   [noahtheduke.splint.utils :refer [drop-quote simple-type]]))

(set! *warn-on-reflection* true)

(def special? #{:?+ :?* :?? :?|})

(defn literal?
  "Is a given simple-type a literal/non-special?"
  [t]
  (#{:nil :boolean :char :number :keyword :string :symbol :quote :list :map :set} t))

(defn read-dispatch-symbol
  "Return the special type of a special symbol, or :symbol if normal."
  [sym]
  (let [sym-name (name sym)
        char0 (.charAt sym-name 0)]
    (case [char0
           (when (< 1 (count sym-name))
             (.charAt sym-name 1))]
      ([\_ nil] [\? \_]) :any
      [\? nil] :?
      [\? \+] :?+
      [\? \*] :?*
      [\? \?] :??
      [\? \|] :?|
      ; else
      (if (identical? \? char0)
        :?
        :symbol))))

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
         :symbol (read-dispatch-symbol pattern)
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

#_(remove-all-methods read-form)

(defmethod read-form :default
  read-form--default
  [ctx pattern form]
  `(throw (ex-info "default" {:type ~(read-dispatch ctx pattern form)
                              :pattern '~pattern})))

(defmethod read-form :?*
  read-form--?*
  [_ctx _pattern _form]
  (throw (IllegalArgumentException. "`:?*` must be used in a surrounding sequence")))

(defmethod read-form :?+
  read-form--?*
  [_ctx _pattern _form]
  (throw (IllegalArgumentException. "`:?+` must be used in a surrounding sequence")))

(defmethod read-form :??
  read-form--?*
  [_ctx _pattern _form]
  (throw (IllegalArgumentException. "`:??` must be used in a surrounding sequence")))

(defmethod read-form :?|
  read-form--?*
  [_ctx _pattern _form]
  (throw (IllegalArgumentException. "`:?|` must be used in a surrounding sequence")))

(defmethod read-form :any
  read-form--any
  [ctx _pattern _form]
  ctx)

(defmethod read-form :nil
  read-form--nil
  [ctx _pattern form]
  `(when (nil? ~form)
     ~ctx))

(defmethod read-form :boolean
  read-form--boolean
  [ctx pattern form]
  `(when (identical? ~pattern ~form)
     ~ctx))

(defmethod read-form :char
  read-form--char
  [ctx pattern form]
  `(when (identical? ~pattern ~form)
     ~ctx))

(defmethod read-form :number
  read-form--number
  [ctx pattern form]
  `(when (or (identical? ~pattern ~form) (= ~pattern ~form))
     ~ctx))

(defmethod read-form :keyword
  read-form--keyword
  [ctx pattern form]
  `(when (identical? ~pattern ~form)
     ~ctx))

(defmethod read-form :string
  read-form--string
  [ctx pattern form]
  `(when (.equals ^String ~pattern ~form)
     ~ctx))

(defmethod read-form :symbol
  read-form--symbol
  [ctx pattern form]
  `(when (= '~pattern ~form)
     ~ctx))

(defmethod read-form :quote
  read-form--quote
  [ctx pattern form]
  (let [children-form (gensym "quote-form-")
        interior-pattern (second pattern)
        interior-pattern (vary-meta* interior-pattern assoc :splint/lit true)]
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
         (if-let [^clojure.lang.MapEntry existing# (find ~ctx '~bind)]
           (when (= (val existing#) ~children-form)
             ~ctx)
           (assoc ~ctx '~bind ~children-form))))))

(defn match-pred
  [ctx bind form pred]
  (let [children-form (gensym "pred-form-")]
    `(let [~children-form ~form]
       (when (~pred ~children-form)
         ~(match-binding ctx bind children-form)))))

(defn coerce-bind
  [bind]
  (if (str/starts-with? (str bind) "?")
    bind
    (symbol (str "?" bind))))

(defmethod read-form :?
  read-form--?
  [ctx pattern form]
  (let [pattern (if (symbol? pattern) ['? pattern] pattern)
        [_?sym ?bind pred] pattern
        bind (coerce-bind ?bind)]
    (cond
      (not (#{2 3} (count pattern)))
      (throw (IllegalArgumentException. "? only accepts 1 or 2 arguments"))
      (and (= 3 (count pattern)) (not (symbol? pred)))
      (throw (IllegalArgumentException. "? pred must be a symbol"))
      (nil? pred)
      (match-binding ctx bind form)
      (symbol? pred)
      (match-pred ctx bind form pred)
      :else
      (throw (ex-info "Predicate must be a symbol" {:pred pred})))))

(defn match-star-rest
  [ctx pattern]
  (let [[_?sym bind pred] pattern
        body-form (gensym "star-rest-form-")
        pred-check (if pred `(every? ~pred ~body-form) true)]
    (when-not (#{2 3} (count pattern))
      (throw (IllegalArgumentException. "?* only accepts 1 or 2 arguments")))
    (when (and (= 3 (count pattern)) (not (symbol? pred)))
      (throw (IllegalArgumentException. "?* pred must be a symbol")))
    [(gensym "star-rest-fn-")
     `(fn [~ctx form# cont#]
        (let [~body-form (vary-meta (vec form#) assoc ::rest true)]
          (when ~pred-check
            (let [~ctx ~(match-binding ctx (coerce-bind bind) body-form)]
              (cont# ~ctx nil)))))]))

(defn match-star
  [ctx pattern]
  (let [[_?sym bind pred] pattern
        body-form (gensym "star-form-")
        pred-check (if pred `(every? ~pred ~body-form) true)]
    (when-not (#{2 3} (count pattern))
      (throw (IllegalArgumentException. "?* only accepts 1 or 2 arguments")))
    (when (and (= 3 (count pattern)) (not (symbol? pred)))
      (throw (IllegalArgumentException. "?* pred must be a symbol")))
    [(gensym "star-fn-")
     `(fn [~ctx form# cont#]
        (let [max-len# (count form#)]
          (loop [i# 0
                 ~body-form (vary-meta (vec (take i# form#)) assoc ::rest true)]
            (when (<= i# max-len#)
              (or (and ~pred-check
                    (let [~ctx ~(match-binding ctx (coerce-bind bind) body-form)]
                      (cont# ~ctx (drop i# form#))))
                (recur (inc i#)
                  (if (< (count ~body-form) max-len#)
                    (conj ~body-form (nth form# (count ~body-form)))
                    ~body-form)))))))]))

(defn match-plus-rest
  [ctx pattern]
  (let [[_?sym bind pred] pattern
        body-form (gensym "plus-rest-form-")
        pred-check (if pred `(every? ~pred ~body-form) true)]
    (when-not (#{2 3} (count pattern))
      (throw (IllegalArgumentException. "?+ only accepts 1 or 2 arguments")))
    (when (and (= 3 (count pattern)) (not (symbol? pred)))
      (throw (IllegalArgumentException. "?+ pred must be a symbol")))
    [(gensym "plus-rest-fn-")
     `(fn [~ctx form# cont#]
        (when (seq form#)
          (let [~body-form (vary-meta (vec form#) assoc ::rest true)]
            (when ~pred-check
              (let [~ctx ~(match-binding ctx (coerce-bind bind) body-form)]
                (cont# ~ctx nil))))))]))

(defn match-plus
  [ctx pattern]
  (let [[_?sym bind pred] pattern
        body-form (gensym "plus-form-")
        pred-check (if pred `(every? ~pred ~body-form) true)]
    (when-not (#{2 3} (count pattern))
      (throw (IllegalArgumentException. "?+ only accepts 1 or 2 arguments")))
    (when (and (= 3 (count pattern)) (not (symbol? pred)))
      (throw (IllegalArgumentException. "?+ pred must be a symbol")))
    [(gensym "plus-fn-")
     `(fn [~ctx form# cont#]
        (let [max-len# (count form#)]
          (loop [i# 1
                 ~body-form (vary-meta (vec (take i# form#)) assoc ::rest true)]
            (when (<= i# max-len#)
              (or (and ~pred-check
                    (let [~ctx ~(match-binding ctx (coerce-bind bind) body-form)]
                      (cont# ~ctx (drop i# form#))))
                (recur (inc i#)
                  (if (< (count ~body-form) max-len#)
                    (conj ~body-form (nth form# (count ~body-form)))
                    ~body-form)))))))]))

(defn match-optional
  [ctx pattern]
  (let [[_?sym bind pred] pattern
        body-form (gensym "optional-form-")
        pred-check (if (= 3 (count pattern))
                     `(every? ~pred ~body-form)
                     true)]
    (when-not (#{2 3} (count pattern))
      (throw (IllegalArgumentException. "?? only accepts 1 or 2 arguments")))
    (when (and (= 3 (count pattern)) (not (symbol? pred)))
      (throw (IllegalArgumentException. "?? pred must be a symbol")))
    [(gensym "optional-fn-")
     `(fn [~ctx form# cont#]
        (let [~body-form (vary-meta () assoc ::rest true)]
          (or (and ~pred-check
                (let [ctx# ~(match-binding ctx (coerce-bind bind) body-form)]
                  (cont# ctx# form#)))
            (when (seq form#)
              (let [~body-form (vary-meta (vec (take 1 form#)) assoc ::rest true)]
                (when ~pred-check
                  (let [ctx# ~(match-binding ctx (coerce-bind bind) body-form)]
                    (cont# ctx# (drop 1 form#)))))))))]))

(defn match-alt
  [ctx pattern]
  (let [[_?sym bind alts] pattern]
    (when-not (= 3 (count pattern))
      (throw (IllegalArgumentException. "?| only accepts 2 arguments")))
    (when-not (and (vector? alts)
                (seq alts)
                (every? #(literal? (read-dispatch %)) alts))
      (throw (IllegalArgumentException. "?| alts must be a vector of literals")))
    (let [temp-ctx (gensym "temp-ctx-")
          body-form (gensym "alt-body-form-")
          binds [temp-ctx
                 `(let [~body-form (first ~body-form)]
                    (when ((quote ~(set alts)) ~body-form)
                      ~(match-binding ctx (coerce-bind bind) body-form)))
                 body-form
                 `(if ~temp-ctx
                    (next ~body-form)
                    ~body-form)]]
      [(gensym "alt-fn-")
       `(fn [~ctx ~body-form cont#]
          (let [~temp-ctx nil
                ~@binds
                ~ctx ~temp-ctx]
            (when ~ctx
              (cont# ~ctx ~body-form))))])))

(defn- match-single-binds
  [ctx children-form items]
  (mapcat
    (fn [item]
      [ctx `(when (and ~ctx (seq ~children-form))
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
        pattern-pairs (update pattern-pairs
                        (dec (count pattern-pairs))
                        vary-meta* assoc ::last true)
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
                      (mapcat #(if (::last (meta %))
                                 (match-plus-rest ctx %)
                                 (match-plus ctx %))
                        patterns)
                      :?*
                      (mapcat #(if (::last (meta %))
                                 (match-star-rest ctx %)
                                 (match-star ctx %))
                        patterns)
                      :??
                      (mapcat #(match-optional ctx %) patterns)
                      :?|
                      (mapcat #(match-alt ctx %) patterns)
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

(defmethod read-form :list
  read-form--list
  [ctx pattern form]
  (seq-match ctx pattern form))

(defmethod read-form :vector
  read-form--vector
  [ctx pattern form]
  (seq-match ctx pattern form))

(defn non-coll?
  "Is a given simple-type a non-collection?"
  [t]
  (#{:nil :boolean :char :number :keyword :string :symbol} t))

(defmethod read-form :map
  read-form--map
  [ctx pattern form]
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

(defmethod read-form :set
  read-form--set
  [ctx pattern form]
  {:pre [(every? (comp non-coll? simple-type) pattern)]}
  (let [new-form (gensym "set-form-")
        binds (mapcat
                (fn [v]
                  [ctx `(when (contains? ~new-form ~v)
                          ~ctx)])
                pattern)]
    `(let [~new-form ~form]
       (and (set? ~new-form)
         (<= ~(count (keys pattern)) (count ~new-form))
         (let [~@binds]
           ~ctx)))))

(defn expand-specials [pattern]
  (postwalk*
    (fn [obj]
      (if (symbol? obj)
        (let [special-type (read-dispatch-symbol obj)]
          (case special-type
            :any '_
            (:symbol :?) obj
            (:?+ :?* :??) (let [sym (symbol special-type)]
                            (if (= sym obj)
                              obj
                              (list sym (symbol (subs (name obj) 2)))))
            :?| (if (= '?| obj)
                  obj
                  (throw (IllegalArgumentException. "Can't use ?| on a symbol")))
            ; else
            (throw (IllegalArgumentException. (str "Unreachable, found with " obj)))))
        obj))
    pattern))

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
        ctx (gensym "ctx-")
        pattern' (-> pattern
                   (drop-quote)
                   (expand-specials))]
    `(fn [~form]
       (let [~ctx {}]
         (or ~(read-form ctx pattern' form) nil)))))
