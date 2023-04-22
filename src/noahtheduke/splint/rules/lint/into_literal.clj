; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.lint.into-literal
  (:require
    [noahtheduke.splint.diagnostic :refer [->diagnostic]]
    [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defn set-or-vec? [form]
  (and (or (set? form)
           (vector? form))
       (empty? form)))

(defrule lint/into-literal
  "`vec` and `set` are succinct and meaningful.

  Examples:

  ; bad
  (into [] coll)

  ; good
  (vec coll)

  ; bad
  (into #{} coll)

  ; good
  (set coll)
  "
  {:pattern '(into %set-or-vec?%-?literal ?coll)
   :on-match (fn [ctx rule form {:syms [?literal ?coll]}]
               (let [replace-form (list (if (set? ?literal) 'set 'vec) ?coll)
                     message (format "Use `%s` instead of recreating it."
                                     (if (set? ?literal) "set" "vec"))]
                 (->diagnostic rule form {:replace-form replace-form
                                          :message message})))})
