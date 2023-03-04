; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.lint.next-first
  (:require
    [noahtheduke.splint.rules :refer [defrule]]))

(defrule next-first
  "nfirst is succinct and meaningful.

  Examples:

  ; bad
  (next (first coll))

  ; good
  (nfirst coll)
  "
  {:pattern '(next (first ?coll))
   :message "Use `nfirst` instead of recreating it."
   :replace '(nfirst ?coll)})
