; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.lint.minus-zero
  (:require
    [noahtheduke.splint.rules :refer [defrule]]))

(defrule minus-zero
  "Checks for x - 0.

  Examples:

  ; bad
  (- x 0)

  ; good
  x
  "
  {:pattern '(- ?x 0)
   :message "Subtracting 0 is a no-op."
   :replace '?x})
