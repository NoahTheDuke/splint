; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.style.next-first
  (:require
   [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defrule style/next-first
  "`nfirst` is succinct and meaningful.

  @examples

  ; avoid
  (next (first coll))

  ; prefer
  (nfirst coll)
  "
  {:pattern '(next (first ?coll))
   :message "Use `nfirst` instead of recreating it."
   :autocorrect true
   :replace '(nfirst ?coll)})
