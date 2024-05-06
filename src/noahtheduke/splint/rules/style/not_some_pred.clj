; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.style.not-some-pred
  (:require
   [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defrule style/not-some-pred
  "not-any? is succinct and meaningful.

  Examples:

  ; avoid
  (not (some even? coll))

  ; prefer
  (not-any? even? coll)
  "
  {:pattern '(not (some ?pred ?coll))
   :message "Use `not-any?` instead of recreating it."
   :replace '(not-any? ?pred ?coll)})
