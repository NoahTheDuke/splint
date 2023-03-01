; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.spat.rules.lint.divide-by-one
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defrule divide-by-one
  "Checks for (/ x 1).

  Examples:

  # bad
  (/ x 1)

  # good
  x
  "
  {:pattern '(/ ?x 1)
   :message "Dividing by 1 is a no-op."
   :replace '?x})
