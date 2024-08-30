; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.style.plus-zero
  (:require
   [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defrule style/plus-zero
  "Checks for x + 0.

  @examples

  ; avoid
  (+ x 0)
  (+ 0 x)

  ; prefer
  x
  "
  {:patterns ['(+ ?x 0)
              '(+ 0 ?x)]
   :message "Adding 0 is a no-op."
   :autocorrect true
   :replace '?x})
