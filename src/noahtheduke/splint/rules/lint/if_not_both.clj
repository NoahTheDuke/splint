; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.lint.if-not-both
  (:require
    [noahtheduke.splint.rules :refer [defrule]]))

(defrule if-not-both
  "`if-not` exists, so use it.

  Examples:

  ; bad
  (if (not x) y z)

  ; good
  (if-not x y z)
  "
  {:pattern '(if (not ?x) ?y ?z)
   :message "Use `if-not` instead of recreating it."
   :replace '(if-not ?x ?y ?z)})
