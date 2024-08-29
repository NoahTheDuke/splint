; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.style.first-next
  (:require
   [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defrule style/first-next
  "`fnext` is succinct and meaningful.

  @examples

  ; avoid
  (first (next coll))

  ; prefer
  (fnext coll)
  "
  {:pattern '(first (next ?coll))
   :message "Use `fnext` instead of recreating it."
   :replace '(fnext ?coll)})
