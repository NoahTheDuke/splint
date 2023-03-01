; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.spat.rules.lint.loop-empty-when
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defrule loop-empty-when
  "Empty loops with nested when can be `while`.

  Examples:

  # bad
  (loop [] (when (some-func) (println 1) (println 2) (recur)))

  # good
  (while (some-func) (println 1) (println 2) (recur))
  "
  {:pattern '(loop [] (when ?test &&. ?exprs (recur)))
   :message "Use `while` instead of recreating it."
   :replace '(while ?test &&. ?exprs)})
