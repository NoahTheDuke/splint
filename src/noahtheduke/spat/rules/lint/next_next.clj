; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.spat.rules.lint.next-next
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defrule next-next
  "nnext is succinct and meaningful.

  Examples:

  ; bad
  (next (next coll))

  ; good
  (nnext coll)
  "
  {:pattern '(next (next ?coll))
   :message "Use `nnext` instead of recreating it."
   :replace '(nnext ?coll)})
