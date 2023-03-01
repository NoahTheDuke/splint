; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.spat.rules.lint.if-else-nil
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defrule if-else-nil
  "Idiomatic `if` defines both branches. `when` returns `nil` in the else branch.

  Examples:

  # bad
  (if (some-func) :a nil)

  # good
  (when (some-func) :a)
  "
  {:pattern '(if ?x ?y nil)
   :message "Use `when` which doesn't require specifying the else branch."
   :replace '(when ?x ?y)})
