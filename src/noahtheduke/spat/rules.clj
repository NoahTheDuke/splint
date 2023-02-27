; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.spat.rules
  (:require
    [clojure.walk :as walk]
    [noahtheduke.spat.pattern :refer [pattern simple-type]]))

(set! *warn-on-reflection* true)

(def global-rules
  "All registered rules, grouped by :init-type"
  (atom {}))

(defn postwalk-splicing-replace [binds replace-form]
  (walk/postwalk
    (fn [item]
      (cond
        (seq? item)
        (let [[front-sexp rest-sexp] (split-with #(not= '&&. %) item)]
          (concat front-sexp (second rest-sexp) (drop 2 rest-sexp)))
        (contains? binds item) (binds item)
        :else
        item))
    replace-form))

(defmacro defrule
  [rule-name & opts]
  (let [docs (when (string? (first opts)) [(first opts)])
        opts (if (string? (first opts)) (next opts) opts)
        {pat :pattern :keys [patterns replace on-match message]} opts]
    (assert (simple-symbol? rule-name) "defrule name cannot be namespaced")
    (assert (or pat patterns)
            "defrule must define either :pattern or :patterns")
    (assert (not (and pat patterns))
            "defrule cannot define both :pattern and :patterns")
    (when patterns
      (assert (apply = (map simple-type patterns))
              "All :patterns should have the same `simple-type`"))
    (assert (or replace on-match)
            "defrule must define either :replace or :on-match")
    (assert (not (and replace on-match))
            "defrule cannot define both :replace and :on-match")
    `(let [rule# {:name ~(str rule-name)
                  :docstring ~@docs
                  :init-type (if ~pat
                               (simple-type ~pat)
                               (simple-type (first ~patterns)))
                  :pattern-raw ~(or pat patterns)
                  :replace-raw ~replace
                  :message ~message
                  :pattern (when ~(some? pat) (pattern ~pat))
                  :patterns (when ~(some? patterns)
                              ~(mapv #(do (list `pattern %)) patterns))
                  :replace ~(when replace
                              `(fn ~(symbol (str rule-name "-replacer-fn"))
                                 [binds#]
                                 (postwalk-splicing-replace binds# ~replace)))
                  :on-match ~on-match}]
       (swap! global-rules assoc-in
              [~(simple-type (or pat (first patterns))) ~(str rule-name)]
              rule#)
       (def ~rule-name ~@docs rule#))))

(defn ->violation
  [rule form & {:keys [binds message replace-form] :as _opts}]
  (let [form-meta (meta form)
        message (or message (:message rule))
        alt (cond
              replace-form replace-form
              (:replace rule) ((:replace rule) binds))]
    {:rule-name (:name rule)
     :form form
     :message message
     :line (:line form-meta)
     :column (:column form-meta)
     :filename (:filename form-meta)
     :alt alt}))
