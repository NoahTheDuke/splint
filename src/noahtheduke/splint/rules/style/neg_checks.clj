; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.style.neg-checks
  (:require
   [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defrule style/neg-checks
  "`neg?` exists so use it.

  @examples

  ; avoid
  (< num 0)
  (> 0 num)

  ; prefer
  (neg? num)
  "
  {:patterns ['(< ?x 0)
              '(> 0 ?x)]
   :message "Use `neg?` instead of recreating it."
   :replace '(neg? ?x)})
