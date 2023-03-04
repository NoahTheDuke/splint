; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.lint.eq-false
  (:require
    [noahtheduke.splint.rules :refer [defrule]]))

(defrule eq-false
  "`false?` exists so use it.

  Examples:

  ; bad
  (= false x)
  (= x false)

  ; good
  (false? x)
  "
  {:patterns ['(= false ?x)
              '(= ?x false)]
   :message "Use `false?` instead of recreating it."
   :replace '(false? ?x)})
