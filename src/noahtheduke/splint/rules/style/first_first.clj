; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.style.first-first
  (:require
    [noahtheduke.splint.rules :refer [defrule]]))

(defrule style/first-first
  "ffirst is succinct and meaningful.

  Examples:

  ; bad
  (first (first coll))

  ; good
  (ffirst coll)
  "
  {:pattern '(first (first ?coll))
   :message "Use `ffirst` instead of recreating it."
   :replace '(ffirst ?coll)})
