; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.lint.divide-by-one
  (:require
   [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defrule lint/divide-by-one
  "Checks for `(/ x 1)`.

  @examples

  ; avoid
  (/ x 1)

  ; prefer
  x
  "
  {:pattern '(/ ?x 1)
   :message "Dividing by 1 is a no-op."
   :autocorrect true
   :replace '?x})
