; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.spat
  (:require
   [clj-kondo.hooks-api :as hapi]
   [clj-kondo.impl.rewrite-clj.parser :as p]
   [clj-kondo.impl.utils :as u]
   [clojure.string :as str]
   [noahtheduke.core-extensions :refer [postwalk-replace*]]))

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

(comment
  (s-type {:a 1})
  (s-type (Object.)))

(defn read-dispatch [sexp _form _retval]
  (let [type (s-type sexp)]
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

(defmethod read-form :default [sexp form retval]
  `(do (throw (ex-info "default" {:type (read-dispatch ~sexp ~form ~retval)}))
       false))

(defmethod read-form :quote [sexp form retval]
  (read-form (first (next sexp)) form retval))

(defmethod read-form :literal [sexp form _retval]
  `(= ~sexp (:value ~form)))

(defmethod read-form :keyword [sexp form _retval]
  `(= ~sexp (:k ~form)))

(defmethod read-form :string [sexp form _retval]
  `(when-let [lines# (:lines ~form)]
     (= ~(str/split-lines sexp) lines#)))

(defn get-simple-val [form]
  (or (:value form)
      (:k form)
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
        pred (symbol (subs pred 1))
        bind (when bind (symbol bind))]
    `(let [result# ((resolve '~pred) (get-val ~form))]
       (when (and result# '~bind)
         (swap! ~retval assoc '~bind (hapi/sexpr ~form)))
       result#)))

(defmethod read-form :var [sexp form retval]
  `(do (swap! ~retval assoc '~sexp (hapi/sexpr ~form))
       true))

(defmethod read-form :symbol [sexp form _retval]
  `(= '~sexp (:value ~form)))

(defmethod read-form :rest [sexp form retval]
  ;; drop &&.
  (let [sym (symbol (subs (name sexp) 3))
        new-form (gensym (str "rest-new-form-"))]
    `(let [~new-form ~form]
       (swap! ~retval assoc '~sym (hapi/sexpr ~new-form))
         true)))

(defn- read-form-seq [sexp form retval tag]
  (let [children-form (gensym (str (name tag) "-form-"))
        rest? (volatile! false)
        preds (loop [idx 0
                     sexp sexp
                     acc []]
                (if-let [item (first sexp)]
                  ;; flag that we've hit a special rest binding
                  (if (= '&&. item)
                    (do (vreset! rest? true)
                        (let [ret (read-form
                                    (symbol (str "&&." (name (second sexp))))
                                    `(drop ~idx ~children-form)
                                    retval)]
                          (conj acc ret)))
                    (let [res (read-form item `(nth ~children-form ~idx) retval)]
                      (recur (inc idx)
                             (next sexp)
                             (if res (conj acc res) acc))))
                  acc))
        ;; If there's a rest arg, then count of given will be less than or equal
        size-pred (if @rest?
                    `(<= ~(- (count sexp) 2) (count ~children-form))
                    `(= ~(count sexp) (count ~children-form)))
        new-form (gensym (str (name tag) "-new-form-"))]
    `(let [~new-form ~form]
       (and (= ~tag (:tag ~new-form))
            (let [~children-form (:children ~new-form)]
              (and ~size-pred
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

(defmacro pattern
  "Must be wrapped in a function to be useful."
  [form sexp]
  (let [retval (gensym "retval-")]
    `(let [~retval (atom {})]
       (when ~(read-form sexp form retval)
         @~retval))))

(defmacro defrule
  [rule-name & opts]
  (let [docs (when (string? (first opts)) (first opts))
        opts (if (string? (first opts)) (next opts) opts)
        {:keys [pattern replace message replace-fn]} opts]
    (assert (simple-symbol? rule-name) "defrule name cannot be namespaced")
    (assert (and pattern message) "defrule requires a pattern and message")
    (assert (not (and replace replace-fn))
            "defrule cannot define both replace and replace-fn")
    `(def ~rule-name
       {:name ~(str rule-name)
        :docstring ~docs
        :pattern (fn ~(symbol (str rule-name "-pattern-fn"))
                   [form#]
                   (pattern form# ~pattern))
        :message '~message
        :replace (when ~replace
                   (fn ~(symbol (str rule-name "-replacer-fn"))
                     [form#]
                     (when (not-empty form#)
                       (postwalk-replace* form# ~replace))))
        :replace-fn (when ~replace-fn ~replace-fn)
        })))

(defrule str-to-string
  "(.toString) to (str)"
  {:pattern '(.toString ?x)
   :message ""
   :replace '(str ?x)})

(defrule str-apply-interpose
  "(apply str (interpose)) to (str/join)"
  {:pattern '(apply str (interpose ?x ?y))
   :message ""
   :replace '(clojure.string/join ?x ?y)})

(defrule str-apply-reverse
  "(apply str (reverse)) to (str/reverse)"
  {:pattern '(apply str (reverse ?x))
   :message ""
   :replace '(clojure.string/reverse ?x)})

(defrule str-apply-str
  "(apply str) to (str/join)"
  {:pattern '(apply str ?x)
   :message ""
   :replace '(clojure.string/join ?x)}) 

(def string-rules
  "All str and clojure.string related rules"
  [str-to-string
   str-apply-interpose
   str-apply-reverse
   str-apply-str])

(defrule mapcat-apply-apply
  {:pattern '(apply concat (apply map ?x ?y))
   :message ""
   :replace '(mapcat ?x ?y)})

(defrule mapcat-concat-map
  {:pattern '(apply concat (map ?x . ?y))
   :message ""
   :replace '(mapcat ?x . ?y)})

(defrule filter-complement
  {:pattern '(filter (complement ?pred) ?coll)
   :message ""
   :replace '(remove ?pred ?coll)})

(defrule filter-seq
  {:pattern '(filter seq ?coll)
   :message ""
   :replace '(remove empty? ?coll)})

(defrule filter-fn*-not-pred
  {:pattern '(filter (fn* [?x] (not (?pred ?x))) ?coll)
   :message ""
   :replace '(remove ?pred ?coll)})

(defrule filter-fn-not-pred
  {:pattern '(filter (fn [?x] (not (?pred ?x))) ?coll)
   :message ""
   :replace '(remove ?pred ?coll)})

(defrule filter-vec-filter
  {:pattern '(vec (filter ?pred ?coll))
   :message ""
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
   :message ""
   :replace '(ffirst ?coll)})

(defrule first-next
  {:pattern '(first (next ?coll))
   :message ""
   :replace '(fnext ?coll)})

(defrule next-next
  {:pattern '(next (next ?coll))
   :message ""
   :replace '(nnext ?coll)})

(defrule next-first
  {:pattern '(next (first ?coll))
   :message ""
   :replace '(nfirst ?coll)})

(def first-next-rules
  [first-first
   first-next
   next-first
   next-next])

(defrule fn*-wrapper
  {:pattern '(fn* [?arg] (?fun ?arg))
   :message ""
   :replace '?fun})

(defrule fn-wrapper
  {:pattern '(fn [?arg] (?fun ?arg))
   :message ""
   :replace '?fun})

(def fn-rules
  [fn*-wrapper
   fn-wrapper])

(defrule thread-first-no-arg
  "(-> x) to x"
  {:pattern '(-> ?x)
   :message ""
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
   :message ""
   :replace-fn (fn [{:syms [?arg ?form]}]
                 (if (list? ?form)
                   (list* (first ?form) ?arg (rest ?form))
                   (list ?form ?arg)))})

(defrule thread-last-no-arg
  "(->> x) to x"
  {:pattern '(->> ?x)
   :message ""
   :replace '?x})

(defrule thread-last-1-arg
  "(->> x y) to (y x)
  (->> x (y)) to (y x)"
  {:pattern '(->> ?arg %symbol-or-keyword-or-list?%-?form)
   :message ""
   :replace-fn (fn [{:syms [?arg ?form]}]
                 (if (list? ?form)
                   (list* (concat ?form [?arg]))
                   (list ?form ?arg)))})

(def threading-rules
  [thread-first-no-arg
   thread-first-1-arg
   thread-last-no-arg
   thread-last-1-arg])

(defrule not-some-pred
  {:pattern '(not (some ?pred ?coll))
   :message ""
   :replace '(not-any? ?pred ?coll)})

(defrule with-meta-f-meta
  {:pattern '(with-meta ?x (?f (meta ?x) &&. ?arg))
   :message ""
   :replace '(vary-meta ?x ?f &&. ?arg)})

(def misc-rules
  [not-some-pred
   with-meta-f-meta])

(def all-rules
  (vec (concat string-rules
               sequence-rules
               first-next-rules
               fn-rules
               threading-rules
               misc-rules)))

(defn check-multiple-rules [rules sexp]
  (reduce
    (fn [_ rule]
      (let [pattern (:pattern rule)
            replace (or (:replace rule) (:replace-fn rule))]
        (when-let [result (pattern sexp)]
          (reduced (replace result)))))
    nil
    rules))

(defn check-all-rules [sexp]
  (check-multiple-rules all-rules sexp))

; (let [sexp (p/parse-string "(next (first (range 10)))")]
;   (require '[criterium.core :as cc])
;   (cc/quick-bench (check-all-rules sexp)))


(check-all-rules (p/parse-string "(with-meta {} (+ (meta {}) 1 2 3 4))"))

(comment
  (time (let [{pat :pattern r :replace-fn} thread-first-1-arg]
          (r (pat (p/parse-string "(-> \"hello\" (goodbye heck))")))))
  (time (let [{pat :pattern r :replace-fn} thread-first-1-arg]
          (r (pat (p/parse-string "(-> \"hello\" (goodbye))")))))
  ,)
