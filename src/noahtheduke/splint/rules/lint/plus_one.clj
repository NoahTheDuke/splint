; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.lint.plus-one
  (:require
    [noahtheduke.splint.rules :refer [defrule]]))

(defrule plus-one
  "Checks for simple +1 that should use `clojure.core/inc`.

  Examples:

  ; bad
  (+ x 1)
  (+ 1 x)

  ; good
  (inc x)
  "
  {:patterns ['(+ ?x 1)
              '(+ 1 ?x)]
   :message "Use `inc` instead of recreating it."
   :replace '(inc ?x)})
