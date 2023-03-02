; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.spat.rules.lint.when-not-do
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defrule when-not-do
  "`when-not` already defines an implicit `do`. Rely on it.

  Examples:

  ; bad
  (when-not x (do (println :a) (println :b) :c))

  ; good
  (when-not x (println :a) (println :b) :c)
  "
  {:pattern '(when-not ?x (do &&. ?y))
   :message "Unnecessary `do` in `when-not` body."
   :replace '(when-not ?x &&. ?y)})
