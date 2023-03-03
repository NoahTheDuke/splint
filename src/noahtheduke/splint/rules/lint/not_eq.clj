; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.not-eq
  (:require
    [noahtheduke.splint.rules :refer [defrule]]))

(defrule not-eq
  "`not=` exists, so use it.

  Examples:

  ; bad
  (not (= num1 num2))

  ; good
  (not= num1 num2)
  "
  {:pattern '(not (= &&. ?args))
   :message "Use `not=` instead of recreating it."
   :replace '(not= &&. ?args)})

