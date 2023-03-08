; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.lint.when-not-call
  (:require
    [noahtheduke.splint.rules :refer [defrule]]))

(defrule lint/when-not-call
  "`when-not` exists so use it lol.

  Examples:

  ; bad
  (when (not x) :a :b :c)

  ; good
  (when-not x :a :b :c)
  "
  {:pattern '(when (not ?x) &&. ?y)
   :message "Use `when-not` instead of recreating it."
   :replace '(when-not ?x &&. ?y)})
