; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.style.eq-true
  (:require
   [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defrule style/eq-true
  "`true?` exists so use it.

  @examples

  ; avoid
  (= true x)
  (= x true)

  ; prefer
  (true? x)
  "
  {:patterns ['(= true ?x)
              '(= ?x true)]
   :message "Use `true?` instead of recreating it."
   :autocorrect true
   :replace '(true? ?x)})
