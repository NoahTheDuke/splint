; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.style.not-nil
  (:require
   [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defrule style/not-nil?
  "`some?` exists so use it.

  @examples

  ; avoid
  (not (nil? x))

  ; prefer
  (some? x)
  "
  {:pattern '(not (nil? ?x))
   :message "Use `some?` instead of recreating it."
   :autocorrect true
   :replace '(some? ?x)})
