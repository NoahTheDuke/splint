; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.lint.eq-nil
  (:require
    [noahtheduke.splint.rules :refer [defrule]]))

(defrule lint/eq-nil
  "`nil?` exists so use it.

  Examples:

  ; bad
  (= nil x)
  (= x nil)

  ; good
  (nil? x)
  "
  {:patterns ['(= nil ?x)
              '(= ?x nil)]
   :message "Use `nil?` instead of recreating it."
   :replace '(nil? ?x)})
