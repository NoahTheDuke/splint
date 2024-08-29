; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.style.plus-one
  (:require
   [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defrule style/plus-one
  "Checks for simple +1 that should use `clojure.core/inc`.

  @examples

  ; avoid
  (+ x 1)
  (+ 1 x)

  ; prefer
  (inc x)
  "
  {:patterns ['(+ ?x 1)
              '(+ 1 ?x)]
   :message "Use `inc` instead of recreating it."
   :replace '(inc ?x)})
