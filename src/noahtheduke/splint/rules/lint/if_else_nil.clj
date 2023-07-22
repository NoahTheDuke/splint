; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.lint.if-else-nil
  (:require
    [noahtheduke.splint.diagnostic :refer [->diagnostic]]
    [noahtheduke.splint.rules :refer [defrule]]
    [noahtheduke.splint.rules.helpers :refer [rest-arg?]]))

(set! *warn-on-reflection* true)

(defrule lint/if-else-nil
  "Idiomatic `if` defines both branches. `when` returns `nil` in the else branch.

  Examples:

  ; bad
  (if (some-func) :a nil)

  ; good
  (when (some-func) :a)
  "
  {:patterns2 ['(if ?x ?y nil)
               '(if ?x (do ?*y))
               '(if ?x ?y)]
   :message "Use `when` which doesn't require specifying the else branch."
   :on-match (fn [ctx rule form {:syms [?x ?y]}]
               (let [new-form (if (rest-arg? ?y)
                                (list* 'when ?x ?y)
                                (list 'when ?x ?y))]
                 (->diagnostic ctx rule form {:replace-form new-form})))})
