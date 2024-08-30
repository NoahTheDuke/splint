; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.style.eq-nil
  (:require
   [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defrule style/eq-nil
  "`nil?` exists so use it.

  @examples

  ; avoid
  (= nil x)
  (= x nil)

  ; prefer
  (nil? x)
  "
  {:patterns ['(= nil ?x)
              '(= ?x nil)]
   :message "Use `nil?` instead of recreating it."
   :autocorrect true
   :replace '(nil? ?x)})
