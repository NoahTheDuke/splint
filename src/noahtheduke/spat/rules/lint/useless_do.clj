; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.spat.rules.lint.useless-do
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defrule useless-do
  "A single item in a `do` is a no-op.

  Examples:

  ; bad
  (do coll)

  ; good
  coll"
  {:pattern '(do ?x)
   :message "Unnecessary `do`."
   :replace '?x})
