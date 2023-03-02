; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.spat.rules.lint.not-some-pred
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defrule not-some-pred
  "not-any? is succinct and meaningful.

  ; bad
  (not (some even? coll))

  ; good
  (not-any? even? coll)
  "
  {:pattern '(not (some ?pred ?coll))
   :message "Use `not-any?` instead of recreating it."
   :replace '(not-any? ?pred ?coll)})
