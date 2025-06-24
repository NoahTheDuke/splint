; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.lint.identical-branches
  (:require
   [noahtheduke.splint.diagnostic :refer [->diagnostic]]
   [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defrule lint/identical-branches
  "Both branches of an `if` should not be identical. There's likely a bug in one of the branches.

  @examples

  ; avoid
  (if (pred)
    [1 2 3]
    [1 2 3])
  "
  {:pattern '(if ?_ ?branch ?branch)
   :on-match (fn [ctx rule form _]
               (->diagnostic ctx rule form {:message "Both branches are identical"}))})
