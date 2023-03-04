; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.lint.pos-checks
  (:require
    [noahtheduke.splint.rules :refer [defrule]]))

(defrule pos-checks
  "`pos?` exists so use it.

  Examples:

  ; bad
  (< 0 num)
  (> num 0)

  ; good
  (pos? x)
  "
  {:patterns ['(< 0 ?x)
              '(> ?x 0)]
   :message "Use `pos?` instead of recreating it."
   :replace '(pos? ?x)})
