; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.style.first-first
  (:require
   [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defrule style/first-first
  "ffirst is succinct and meaningful.

  Examples:

  ; avoid
  (first (first coll))

  ; prefer
  (ffirst coll)
  "
  {:pattern '(first (first ?coll))
   :message "Use `ffirst` instead of recreating it."
   :replace '(ffirst ?coll)})
