; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.when-do
  (:require
    [noahtheduke.splint.rules :refer [defrule]]))

(defrule when-do
  "`when` already defines an implicit `do`. Rely on it.

  Examples:

  ; bad
  (when x (do (println :a) (println :b) :c))

  ; good
  (when x (println :a) (println :b) :c)
  "
  {:pattern '(when ?x (do &&. ?y))
   :message "Unnecessary `do` in `when` body."
   :replace '(when ?x &&. ?y)})
