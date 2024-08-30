; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.lint.loop-empty-when
  (:require
   [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defrule lint/loop-empty-when
  "Empty loops with nested `when` can be `while`. Doesn't apply if the final expr of the `when` isn't `(recur)`, which includes any nested cases (`let`, etc).

  @examples

  ; avoid
  (loop [] (when (some-func) (println 1) (println 2) (recur)))

  ; prefer
  (while (some-func) (println 1) (println 2))
  "
  {:pattern '(loop [] (when ?test ?*exprs (recur)))
   :message "Use `while` instead of recreating it."
   :autocorrect true
   :replace '(while ?test ?exprs)})
