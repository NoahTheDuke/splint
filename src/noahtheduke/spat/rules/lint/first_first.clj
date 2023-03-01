; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.spat.rules.lint.first-first
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defrule first-first
  "ffirst is succinct and meaningful.

  Examples:

  # bad
  (first (first coll))

  # good
  (ffirst coll)
  "
  {:pattern '(first (first ?coll))
   :message "Use `ffirst` instead of recreating it."
   :replace '(ffirst ?coll)})
