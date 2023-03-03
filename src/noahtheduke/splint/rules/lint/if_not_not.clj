; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.if-not-not
  (:require
    [noahtheduke.splint.rules :refer [defrule]]))

(defrule if-not-not
  "Two `not`s cancel each other out.

  Examples:

  ; bad
  (if-not (not x) y z)

  ; good
  (if x y z)
  "
  {:pattern '(if-not (not ?x) ?y ?z)
   :message "Use `if` instead of double negation."
   :replace '(if ?x ?y ?z)})
