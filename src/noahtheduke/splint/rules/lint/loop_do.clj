; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.lint.loop-do
  (:require
   [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defrule lint/loop-do
  "`loop` has an implicit `do`. Use it.

  @examples

  ; avoid
  (loop [] (do (println 1) (println 2)))

  ; prefer
  (loop [] (println 1) (println 2))
  "
  {:pattern '(loop ?binding (do ?*exprs))
   :message "Unnecessary `do` in `loop` body."
   :autocorrect true
   :replace '(loop ?binding ?exprs)})
