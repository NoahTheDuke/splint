; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.spat.rules.lint.multiply-by-zero
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defrule multiply-by-zero
  "Checks for (* x 0).

  Examples:

  # bad
  (* x 0)
  (* 0 x)

  # good
  0
  "
  {:patterns ['(* ?x 0)
              '(* 0 ?x)]
   :message "Multiplying by 0 is a no-op."
   :replace '0})
