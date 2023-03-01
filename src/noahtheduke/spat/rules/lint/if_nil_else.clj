; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.spat.rules.lint.if-nil-else
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defrule if-nil-else
  "Idiomatic `if` defines both branches. `when-not` returns `nil` in the truthy branch.

  Examples:

  # bad
  (if (some-func) nil :a)

  # good
  (when-not (some-func) :a)
  "
  {:pattern '(if ?x nil ?y)
   :message "Use `when-not` instead of recreating it."
   :replace '(when-not ?x ?y)})
