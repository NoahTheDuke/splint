; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.lint.redundant-call
  (:require
    [noahtheduke.splint.rules :refer [defrule ->violation]]))

(defn right-fn? [sexp]
  ('#{-> ->>
      cond-> cond->>
      some-> some->>
      comp partial
      merge} sexp))

(defrule redundant-call
  "A number of core functions take any number of arguments and return the arg
  if given only one. These calls are effectively no-ops, redundant, so they
  should be avoided.

  Current list of clojure.core functions this linter checks:
  * `->`, `->>`
  * `cond->`, `cond->>`
  * `some->`, `some->>`
  * `comp`, `partial`, `merge`

  Examples:

  ; bad
  (-> x)
  (->> x)
  (cond-> x)
  (cond->> x)
  (some-> x)
  (some->> x)
  (comp x)
  (partial x)
  (merge x)

  ; good
  x
  "
  {:pattern '(%right-fn?%-?the-fn ?x)
   :on-match (fn [rule form {:syms [?the-fn ?x]}]
               (let [message (format "Single-arg `%s` always returns the arg." ?the-fn)]
                 (->violation rule form {:message message
                                         :replace-form ?x})))})
