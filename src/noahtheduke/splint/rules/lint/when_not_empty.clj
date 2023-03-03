; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.when-not-empty
  (:require
    [noahtheduke.splint.rules :refer [defrule]]))

(defrule when-not-empty?
  "`seq` returns `nil` when given an empty collection. `empty?` is implemented as
  `(not (seq coll))` so it's best and fastest to use `seq` directly.

  Examples:

  ; bad
  (when-not (empty? ?x) &&. ?y)

  ; good
  (when (seq ?x) &&. ?y)
  "
  {:pattern '(when-not (empty? ?x) &&. ?y)
   :message "`seq` is idiomatic, gotta learn to love it."
   :replace '(when (seq ?x) &&. ?y)})
