; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules
  (:require
    [clojure.walk :as walk]
    [noahtheduke.spat.pattern :as p]
    [noahtheduke.splint.diagnostic :refer [->diagnostic]]))

(set! *warn-on-reflection* true)

(def global-rules
  "All registered rules, grouped by :init-type and full-name"
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
  "Define a new rule. Must include:

  * EITHER `:pattern` or `:patterns`,
  * EITHER `:replace` or `:on-match`"
  [rule-name docs opts]
  (let [{:keys [pattern patterns replace on-match message init-type]} opts]
    (assert (qualified-symbol? rule-name) "defrule name must be namespaced")
    (assert (or pattern patterns)
            "defrule must define either :pattern or :patterns")
    (assert (not (and pattern patterns))
            "defrule cannot define both :pattern and :patterns")
    (when patterns
      (assert (vector? patterns) ":patterns must be in a vector")
      (assert (apply = (map p/simple-type patterns))
              "All :patterns should have the same `simple-type`"))
    (assert (or replace on-match)
            "defrule must define either :replace or :on-match")
    (assert (not (and replace on-match))
            "defrule cannot define both :replace and :on-match")
    (let [full-name rule-name
          rule-name (name full-name)
          genre (namespace full-name)
          init-type (or init-type
                        (if pattern
                          (p/simple-type pattern)
                          (p/simple-type (first patterns))))]
      `(let [rule# {:name ~rule-name
                    :genre ~genre
                    :full-name '~full-name
                    :docstring ~docs
                    :init-type ~init-type
                    :pattern-raw ~(or pattern patterns)
                    :replace-raw ~replace
                    :message ~message
                    :pattern (when ~(some? pattern) (p/pattern ~pattern))
                    :patterns (when ~(some? patterns)
                                ~(mapv #(list `p/pattern %) patterns))
                    :on-match
                    ~(or on-match
                         `(fn [rule# form# binds#]
                            (let [new-form# (postwalk-splicing-replace binds# ~replace)]
                              (->diagnostic rule# form# {:replace-form new-form#}))))}]
         (swap! global-rules assoc '~full-name rule#)
         (def ~(symbol rule-name) ~docs rule#)))))
