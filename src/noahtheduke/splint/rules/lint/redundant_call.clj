; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.lint.redundant-call
  (:require
   [noahtheduke.splint.diagnostic :refer [->diagnostic]]
   [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defn right-fn? [sexp]
  (#{'-> '->>
     'cond-> 'cond->>
     'some-> 'some->>
     'comp 'partial 'merge
     'min 'max 'distinct?}
   sexp))

(defn check-parent [ctx]
  (when-let [parent-form (:parent-form ctx)]
    (and (seq? parent-form)
      (#{'case '-> '->> 'cond-> 'cond->> 'some-> 'some->>} (first parent-form)))))

(defrule lint/redundant-call
  "A number of core functions take any number of arguments and return the arg if given only one. These calls are effectively no-ops, redundant, so they should be avoided.

  Current list of clojure.core functions this linter checks:

  * `->`, `->>`
  * `cond->`, `cond->>`
  * `some->`, `some->>`
  * `comp`, `partial`, `merge`
  * `min`, `max`, `distinct?`

  This list can be expanded with the configuration `:fn-names`.

  @examples

  ; avoid
  (-> x)
  (->> x)
  (cond-> x)
  (cond->> x)
  (some-> x)
  (some->> x)
  (comp x)
  (partial x)
  (merge x)
  (min x)
  (max x)
  (distinct? x)

  ; avoid (with `:fn-names [cool-fn]`)
  (cool-fn x)

  ; prefer
  x
  "
  {:pattern '((? fun symbol?) ?x)
   :autocorrect true
   :on-match (fn [ctx rule form {:syms [?fun ?x]}]
               (let [fn-names (:fn-names (:config rule))]
                 (when (and (contains? fn-names (symbol (name ?fun)))
                         (not (check-parent ctx)))
                   (let [message (format "Single-arg `%s` always returns the arg." ?fun)]
                     (->diagnostic ctx rule form {:message message
                                                  :replace-form ?x})))))})
