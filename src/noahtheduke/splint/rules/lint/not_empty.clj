; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.lint.not-empty
  (:require
   [noahtheduke.splint.rules :refer [defrule]]
   [noahtheduke.splint.diagnostic :refer [->diagnostic]]))

(set! *warn-on-reflection* true)

(defn seq-diagnostic [ctx rule form {:syms [?x]}]
  (->diagnostic ctx rule form
    {:message "`seq` is idiomatic, gotta learn to love it."
     :replace-form (list 'seq ?x)}))

(defn not-empty-diagnostic [ctx rule form {:syms [?x]}]
  (->diagnostic ctx rule form
    {:message "`not-empty` is built-in."
     :replace-form (list 'not-empty ?x)}))

(defrule lint/not-empty?
  "`seq` returns `nil` when given an empty collection. `empty?` is implemented as `(not (seq coll))` so it's idiomatic to use `seq` directly.

  @examples

  ; avoid
  (not (empty? coll))

  ; prefer (chosen style :seq (default))
  (seq coll)

  ; prefer (chosen style :not-empty)
  (not-empty coll)
  "
  {:pattern '(not (empty? ?x))
   :autocorrect true
   :on-match (fn [ctx rule form bindings]
               (condp = (:chosen-style (:config rule))
                 :seq (seq-diagnostic ctx rule form bindings)
                 :not-empty (not-empty-diagnostic ctx rule form bindings)))})
