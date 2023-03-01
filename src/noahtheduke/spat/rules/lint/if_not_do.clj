; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.spat.rules.lint.if-not-do
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defrule if-not-do
  "`when-not` already defines an implicit `do`. Rely on it.

  Examples:

  # bad
  (if-not x (do (println :a) (println :b) :c))

  # good
  (if-not x (println :a) (println :b) :c)
  "
  {:pattern '(if-not ?x (do &&. ?y))
   :message "Use `when-not` instead of recreating it."
   :replace '(when-not ?x &&. ?y)})
