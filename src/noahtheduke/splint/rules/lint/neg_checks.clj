; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.lint.neg-checks
  (:require
    [noahtheduke.splint.rules :refer [defrule]]))

(defrule neg-checks
  "`neg?` exists so use it.

  Examples:

  ; bad
  (< num 0)
  (> 0 num)

  ; good
  (neg? x)
  "
  {:patterns ['(< ?x 0)
              '(> 0 ?x)]
   :message "Use `neg?` instead of recreating it."
   :replace '(neg? ?x)})
