; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.style.plus-zero
  (:require
    [noahtheduke.splint.rules :refer [defrule]]))

(defrule style/plus-zero
  "Checks for x + 0.

  Examples:

  ; bad
  (+ x 0)
  (+ 0 x)

  ; good
  x
  "
  {:patterns ['(+ ?x 0)
              '(+ 0 ?x)]
   :message "Adding 0 is a no-op."
   :replace '?x})
