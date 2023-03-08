; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.lint.when-not-not
  (:require
    [noahtheduke.splint.rules :refer [defrule]]))

(defrule lint/when-not-not
  "Two `not`s cancel each other out.

  Examples:

  ; bad
  (when-not (not x) y z)

  ; good
  (when x y z)
  "
  {:pattern '(when-not (not ?x) &&. ?y)
   :message "Use `when` instead of double negation."
   :replace '(when ?x &&. ?y)})
