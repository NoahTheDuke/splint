; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.lint.useless-do
  (:require
    [noahtheduke.splint.rules :refer [defrule]]))

(defrule lint/useless-do
  "A single item in a `do` is a no-op.

  Examples:

  ; bad
  (do coll)

  ; good
  coll"
  {:pattern '(do ?x)
   :message "Unnecessary `do`."
   :replace '?x})
