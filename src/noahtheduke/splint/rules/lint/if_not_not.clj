; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.lint.if-not-not
  (:require
   [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defrule lint/if-not-not
  "Two `not`s cancel each other out.

  @examples

  ; avoid
  (if-not (not x) y z)

  ; prefer
  (if x y z)
  "
  {:pattern '(if-not (not ?x) ?y ?z)
   :message "Use `if` instead of double negation."
   :autocorrect true
   :replace '(if ?x ?y ?z)})
