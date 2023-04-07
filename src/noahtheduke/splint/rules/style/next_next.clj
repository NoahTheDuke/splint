; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.style.next-next
  (:require
    [noahtheduke.splint.rules :refer [defrule]]))

(defrule style/next-next
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
