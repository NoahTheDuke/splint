; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.spat.rules.lint.if-same-truthy
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defrule if-same-truthy
  "`or` exists so use it lol.

  Examples:

  # bad
  (if x x y)

  # good
  (or x y)
  "
  {:pattern '(if ?x ?x ?y)
   :message "Use `or` instead of recreating it."
   :replace '(or ?x ?y)})