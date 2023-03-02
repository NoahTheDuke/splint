; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.spat.rules.lint.not-empty
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defrule not-empty?
  "`seq` returns `nil` when given an empty collection. `empty?` is implemented as
  `(not (seq coll))` so it's best and fastest to use `seq` directly.

  Examples

  ; bad
  (not (empty? coll))

  ; good
  (seq coll)"
  {:pattern '(not (empty? ?x))
   :message "`seq` is idiomatic, gotta learn to love it."
   :replace '(seq ?x)})
