; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.spat.pattern
  (:require
    [clojure.string :as str]))

(set! *warn-on-reflection* true)

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
        retval (gensym "retval-")]
    `(fn [~form]
       (let [~retval (atom {})]
         (when ~(read-form (drop-quote sexp) form retval)
           @~retval)))))

(defmethod read-form :default [sexp form retval]
  `(do (throw (ex-info "default" {:type (read-dispatch ~sexp ~form ~retval)}))
       false))

(defmethod read-form :any [_sexp _form _revtal] nil)

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

(defmethod read-form :symbol [sexp form _retval]
  `(= '~sexp ~form))

(defmethod read-form :pred [sexp form retval]
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

(defn- build-rest-pred [rest-sexp start end children-form retval]
  (let [[_&& rest-sym] rest-sexp]
    (assert rest-sym "&&. needs a follow-up sym")
    `(let [form# (take (- (count ~children-form) ~(+ start end))
                       (drop ~start ~children-form))]
       (if-let [existing# (get @~retval '~rest-sym)]
         (= existing# form#)
         (do (swap! ~retval assoc '~rest-sym form#)
             true)))))

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
  (read-form-seq sexp form retval 'seq?))

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
                            (recur (next complex-keys-preds#)
                                   (vec-remove idx# complex-children#))))))))))))
