; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.spat.rules.lint.loop-do
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defrule loop-do
  "`loop` has an implicit `do`. Use it.

  Examples:

  ; bad
  (loop [] (do (println 1) (println 2)))

  ; good
  (loop [] (println 1) (println 2))
  "
  {:pattern '(loop ?binding (do &&. ?exprs))
   :message "Unnecessary `do` in `loop` body."
   :replace '(loop ?binding &&. ?exprs)})
