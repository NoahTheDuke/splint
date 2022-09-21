; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.spat
  (:require
   [clj-kondo.impl.rewrite-clj.parser :as p]
   [clojure.string :as str]
   [methodical.core :as m]))

(set! *warn-on-reflection* true)

(def example-str
  "(+ 1 2 :k)")

(def example-input
  (p/parse-string example-str))

'{:tag :list,
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

(defn read-dispatch [sexp]
  (cond
    ; literals
    (or (nil? sexp)
        (boolean? sexp)
        (number? sexp)) :literal
    (keyword? sexp) :keyword
    (string? sexp) :string
    (symbol? sexp) :symbol
    ; reader macros
    (map? sexp) :map
    (set? sexp) :set
    (vector? sexp) :vector
    (seq? sexp) (if (= 'quote (first sexp))
                  :quote
                  :list)
    :else (class sexp)))

(m/defmulti read-form read-dispatch)

(m/defmethod read-form :default [sexp]
  (prn :default (read-dispatch sexp)))

(m/defmethod read-form :literal [sexp]
  (fn read-form-literal [form]
    (= sexp (:value form))))

(m/defmethod read-form :keyword [sexp]
  (fn read-form-literal [form]
    (= sexp (:k form))))

(m/defmethod read-form :string [sexp]
  (fn read-form-string [form]
    (when-let [lines (:lines form)]
      (= (str/split-lines sexp) lines))))

(defn get-val [form]
  (or (:value form)
      (:k form)
      (some->> (:lines form) (str/join "\n"))
      (:children form)))

(m/defmethod read-form :symbol [sexp]
  (cond
    (= '_ sexp) any?
    (= \% (first (name sexp)))
    (let [pred (symbol (subs (name sexp) 1))]
      (fn read-form-pred [form]
        ((resolve pred) (get-val form))))
    :else (fn read-form-symbol [form]
            (= sexp (:value form)))))

(m/defmethod read-form :map [sexp]
  (let [{simple-keys true} (group-by #(= :literal %) (map read-dispatch sexp))
        simple-sexp (select-keys sexp simple-keys)
        simple-map (reduce-kv
                     (fn [m k v]
                       (assoc m k (read-form v)))
                     {}
                     simple-sexp)]
    (assert (= (count simple-keys) (count sexp)) "todo complex keys")
    (fn read-form-map [form]
      (let [children (:children form)]
        (and (= :map (:tag form))
             (= (count simple-map) (count children))
             (let [form-map (reduce
                              (fn [m [k v]]
                                (assoc m (get-val k) v))
                              {}
                              (partition 2 children))]
               (reduce-kv
                 (fn [_ k pred]
                   (or (and (contains? form-map k)
                            (pred (get form-map k)))
                       (reduced false)))
                 false
                 simple-map)))))))

(m/defmethod read-form :list [sexp]
  (let [preds (mapv read-form sexp)]
    (fn read-form-list [form]
      (and (= :list (:tag form))
           (= (count preds) (count (:children form)))
           (reduce
             (fn [_ [pred child]]
               (or (pred child)
                   (reduced false)))
             false
             (map vector preds (:children form)))))))

(defn pattern [sexp]
  (read-form sexp))

(comment
  ((pattern '(+ 1 _ %keyword?)) example-input)
  (user/refresh-all)
  ,)
