; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.spat.rules.lint.if-then-do
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defrule if-then-do
  "Each branch of `if` can only have one expression, so using `do` to allow for multiple
  expressions is better expressed with `when`.

  Examples:

  # bad
  (if (some-func) (do (println 1) (println 2)))

  # good
  (when (some-func) (println 1) (println 2))
  "
  {:pattern '(if ?x (do &&. ?y))
   :message "Use `when` instead of recreating it."
   :replace '(when ?x &&. ?y)})
