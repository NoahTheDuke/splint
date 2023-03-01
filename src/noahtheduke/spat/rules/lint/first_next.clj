; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.spat.rules.lint.first-next
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defrule first-next
  "fnext is succinct and meaningful.

  Examples:

  # bad
  (first (next coll))

  # good
  (fnext coll)
  "
  {:pattern '(first (next ?coll))
   :message "Use `fnext` instead of recreating it."
   :replace '(fnext ?coll)})
