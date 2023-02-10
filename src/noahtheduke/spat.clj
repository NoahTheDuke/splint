; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.spat
  (:require
   [clj-kondo.impl.rewrite-clj.parser :as p]
   [clj-kondo.hooks-api :as hapi]
   [clj-kondo.impl.utils :as u]
   [clojure.walk :refer [postwalk-replace]]
   [clojure.string :as str]))

(set! *warn-on-reflection* true)

(defn s-type [sexp]
  (cond
    ; literals
    (or (nil? sexp)
        (boolean? sexp)
        (char? sexp)
        (number? sexp)) :literal
    (keyword? sexp) :keyword
    (string? sexp) :string
    (symbol? sexp) :symbol
    ; reader macros
    (map? sexp) :map
    (set? sexp) :set
    (vector? sexp) :vector
    (seq? sexp) :list
    :else (class sexp)))

(defn read-dispatch [sexp _form _retval]
  (let [type (s-type sexp)]
    (case type
      :symbol (cond
                (= '_ sexp) :any
                (= \% (first (name sexp))) :pred
                (= \? (first (name sexp))) :var
                :else :symbol)
      :list (if (= 'quote (first sexp))
              :quote
              :list)
      ;; else
      type)))

(defmulti read-form #'read-dispatch)

(defmethod read-form :default [sexp form retval]
  `(do (throw (ex-info "default" {:type (read-dispatch ~sexp ~form ~retval)}))
       false))

(defmethod read-form :quote [sexp form retval]
  (read-form (first (next sexp)) form retval))

(defmethod read-form :literal [sexp form retval]
  `(= ~sexp (:value ~form)))

(defmethod read-form :keyword [sexp form retval]
  `(= ~sexp (:k ~form)))

(defmethod read-form :string [sexp form retval]
  `(when-let [lines# (:lines ~form)]
     (= ~(str/split-lines sexp) lines#)))

(defn get-simple-val [form]
  (or (:value form)
      (:k form)
      (some->> (:lines form) (str/join "\n"))))

(defn get-val [form]
  (or (get-simple-val form)
      (:children form)))

(defmethod read-form :any [_sexp _form revtal] nil)

(defmethod read-form :pred [sexp form retval]
  ;; pred has to be unquoted and then quoted to keep it a simple keyword,
  ;; not fully qualified, as it needs to be resolved in the calling namespace
  ;; to allow for custom predicate functions
  (let [pred (symbol (subs (name sexp) 1))]
    `((resolve '~pred) (get-val ~form))))

(defmethod read-form :var [sexp form retval]
  `(do (swap! ~retval assoc '~sexp (hapi/sexpr ~form))
       true))

(defmethod read-form :symbol [sexp form retval]
  `(= '~sexp (:value ~form)))

(defn- read-form-seq [sexp form retval tag]
  (let [children-form (gensym (str (name tag) "-form-"))
        preds (keep-indexed
                (fn [idx item]
                  (read-form item `(nth ~children-form ~idx) retval))
                sexp)
        new-form (gensym (str (name tag) "-new-form-"))]
    `(let [~new-form ~form]
       (and (= ~tag (:tag ~new-form))
            (let [~children-form (:children ~new-form)]
              (and (= ~(count sexp) (count ~children-form))
                   ~@preds))))))

(defmethod read-form :list [sexp form retval]
  (read-form-seq sexp form retval :list))

(defmethod read-form :vector [sexp form retval]
  (read-form-seq sexp form retval :vector))

(def simple? #{:literal :keyword :string :symbol :token})

(defmethod read-form :map [sexp form retval]
  {:pre [(every? (comp simple? s-type) (keys sexp))]}
  (let [children-form (gensym "map-form-children-")
        c-as-map (gensym "map-form-as-map")
        simple-keys (filterv #(simple? (s-type %)) (keys sexp))
        simple-keys-preds (->> (select-keys sexp simple-keys)
                               (mapcat (fn [[k v]]
                                         [(list `contains? c-as-map k)
                                          (read-form v (list `get c-as-map k) retval)])))
        new-form (gensym "map-new-form-")]
    `(when-let [~new-form ~form]
       (and (= :map (:tag ~new-form))
            (let [~children-form (:children ~new-form)]
              (and (every? #(simple? (u/tag %)) (take-nth 2 ~children-form))
                   (let [~c-as-map (->> ~children-form
                                        (partition 2)
                                        (map (fn [[k# v#]] [(get-val k#) v#]))
                                        (into {}))]
                     (and (<= ~(count simple-keys) (count ~c-as-map))
                          ~@simple-keys-preds))))))))

(defn vec-remove
  "remove elem in coll
  from: https://stackoverflow.com/a/18319708/3023252"
  [pos coll]
  (vec (concat (subvec coll 0 pos) (subvec coll (inc pos)))))

(defmethod read-form :set [sexp form retval]
  (let [children-form (gensym "set-form-children-")
        simple-vals-set (gensym "set-form-simple-vals-")
        [simple-vals complex-vals] (reduce (fn [acc cur]
                                             (if (simple? (s-type cur))
                                              (update acc 0 conj cur)
                                              (update acc 1 conj cur)))
                                           [[] []]
                                           sexp)
        simple-keys-preds (map (fn [k] (list `contains? simple-vals-set k)) simple-vals)
        current-child (gensym "set-current-child-")
        complex-keys-preds (mapv (fn [k]
                                   `(fn [~current-child]
                                      ~(read-form k current-child retval)))
                                complex-vals)
        new-form (gensym "set-new-form-")]
    `(when-let [~new-form ~form]
       (and (= :set (:tag ~new-form))
            (let [~children-form (vec (:children ~new-form))]
              (and (<= ~(count sexp) (count ~children-form))
                   (let [~simple-vals-set (set (keep get-simple-val ~children-form))]
                     (and (or ~(empty? simple-keys-preds)
                              (and ~@simple-keys-preds))
                          (or ~(empty? complex-keys-preds)
                              ;; loop over both the predicates and the children.
                              ;; for each predicate, compare it against each child
                              ;; until it finds a match, and then remove the child
                              ;; from the list of children and recur.
                              (loop [complex-keys-preds# (seq ~complex-keys-preds)
                                     complex-children#
                                     (vec (for [child# ~children-form
                                                :when (not (contains? ~simple-vals-set (get-simple-val child#)))]
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
                                          (recur (next complex-keys-preds#) (vec-remove idx# complex-children#))))))))))))))))

(defmacro pattern [sexp]
  (let [form (gensym "form-")
        retval (gensym "retval-")]
    `(fn [~form]
       (let [~retval (atom {})]
         (when ~(read-form sexp form retval)
           @~retval)))))

(defmacro defrule
  [name given replacement]
  `(def ~name
     {:name ~name
      :pattern (pattern '~given)
      :replace #(when (not-empty %)
                  (postwalk-replace % '~replacement))}))

(defrule thread-first (-> ?x) ?x)

(defrule to-string (.toString ?x) (str ?x))

(comment
  (time (let [{pat :pattern r :replace} to-string]
          (r (pat (p/parse-string "(.toString \"hello\")")))))
  ,)
