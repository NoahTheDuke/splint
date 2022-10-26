; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.spat
  (:require
   [clj-kondo.impl.rewrite-clj.parser :as p]
   [clj-kondo.impl.utils :as u]
   [clojure.string :as str]
   [methodical.core :as m]))

(set! *warn-on-reflection* true)

#_'{:tag :list,
    :format-string "(%s)",
    :wrap-length 2,
    :seq-fn f
    :children
    ({:value +, :string-value "+"}
     {:value nil, :string-value "nil"}
     {:value true, :string-value "true"}
     {:value 1, :string-value "1"}
     {:lines ["a"]}
     {:k :b, :namespaced? nil}
     {:tag :quote,
      :prefix "'",
      :sym quote,
      :children [{:value c, :string-value "c"}]}
     {:tag :map,
      :format-string "{%s}",
      :wrap-length 2,
      :seq-fn f
      :children
      ({:tag :list,
        :format-string "(%s)",
        :wrap-length 2,
        :seq-fn f
        :children ({:value 1, :string-value "1"})}
       {:value 2, :string-value "2"})}
     {:tag :set,
      :format-string "#{%s}",
      :wrap-length 3,
      :seq-fn f
      :children ({:value 3, :string-value "3"})})}

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

(defn read-dispatch [sexp _form]
  (let [type (s-type sexp)]
    (case type
      :symbol (cond
                (= '_ sexp) :any
                (= \% (first (name sexp))) :pred
                :else :symbol)
      :list (if (= 'quote (first sexp))
              :quote
              :list)
      type)))

(m/defmulti read-form read-dispatch)

(m/defmethod read-form :default [sexp form]
  (prn :default (read-dispatch sexp form)))

(m/defmethod read-form :literal [sexp form]
  `(= ~sexp (:value ~form)))

(m/defmethod read-form :keyword [sexp form]
  `(= ~sexp (:k ~form)))

(m/defmethod read-form :string [sexp form]
  `(when-let [lines# (:lines ~form)]
     (= ~(str/split-lines sexp) lines#)))

(defn get-val [form]
  (or (:value form)
      (:k form)
      (some->> (:lines form) (str/join "\n"))
      (:children form)))

(m/defmethod read-form :any [_sexp _form] nil)

(m/defmethod read-form :pred [sexp form]
  ;; pred has to be unquoted and then quoted to keep it a simple keyword,
  ;; not fully qualified, as it needs to be resolved in the calling namespace
  ;; to allow for custom predicate functions
  (let [pred (symbol (subs (name sexp) 1))]
    `((resolve '~pred) (get-val ~form))))

(m/defmethod read-form :symbol [sexp form]
  `(= '~sexp (:value ~form)))

(defn- read-form-seq [sexp form tag]
  (let [children-form (gensym (str (name tag) "-form-"))
        preds (keep-indexed
                (fn [idx item]
                  (read-form item `(nth ~children-form ~idx)))
                sexp)]
    `(and (= ~tag (:tag ~form))
          (let [~children-form (:children ~form)]
            (and (= ~(count sexp) (count ~children-form))
                 ~@preds)))))

(m/defmethod read-form :list [sexp form]
  (read-form-seq sexp form :list))

(m/defmethod read-form :vector [sexp form]
  (read-form-seq sexp form :vector))

(def simple? #{:literal :keyword :string :symbol})

(m/defmethod read-form :map [sexp form]
  (let [children-form (gensym "map-form-")
        ; c-as-map (gensym "map-form-child-")
        ; _ (prn :keys (keys sexp))
        simple-keys (filterv (comp simple? s-type) (keys sexp))
        ; _ (prn :simple-keys simple-keys (select-keys sexp simple-keys))
        simple-keys-preds (->> (select-keys sexp simple-keys)
                               (mapcat (fn [[k v]] [`(contains? ~children-form ~k)
                                                    (read-form v `(get ~children-form ~k))])))
        ]
    (prn :simple-keys-preds simple-keys-preds)
    `(let [form# ~form]
       (prn form# (keys form#))
       (let [~children-form (vec (:children form#))]
         (prn :map (= :map (:tag form#))
              :size (<= ~(count simple-keys) (/ (count ~children-form) 2))
              :preds ~@simple-keys-preds
              )
            )
       (and (= :map (:tag form#))
            (let [~children-form (vec (:children form#))]
              (and (<= ~(count simple-keys) (/ (count ~children-form) 2))
                   ~@simple-keys-preds)
              )
            ))))

(defmacro pattern [sexp]
  (let [form (gensym "form-")]
    `(fn [~form] ~(read-form sexp form))))

(comment
  (let [pat (pattern {:a (2 3 4)})]
    (pat (p/parse-string "{:a (2 3 4)}")))
  (user/refresh-all)
  ,)
