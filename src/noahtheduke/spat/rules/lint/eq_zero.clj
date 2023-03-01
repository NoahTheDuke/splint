; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.spat.rules.lint.eq-zero
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defn eq? [sexp]
  (#{'= '==} sexp))

(defrule eq-zero
  "`zero?` exists so use it.

  Examples:

  # bad
  (= 0 num)
  (= num 0)
  (== 0 num)
  (== num 0)

  # good
  (zero? num)
  "
  {:patterns ['(%eq? 0 ?x)
              '(%eq? ?x 0)]
   :message "Use `zero?` instead of recreating it."
   :replace '(zero? ?x)})

