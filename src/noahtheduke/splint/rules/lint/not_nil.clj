; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.not-nil
  (:require
    [noahtheduke.splint.rules :refer [defrule]]))

(defrule not-nil?
  "`some?` exists so use it.

  Examples:

  ; bad
  (not (nil? x))

  ; good
  (some? x)
  "
  {:pattern '(not (nil? ?x))
   :message "Use `some?` instead of recreating it."
   :replace '(some? ?x)})
