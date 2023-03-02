; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.spat.rules.lint.take-repeatedly
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defrule take-repeatedly
  "`repeatedly` has an arity for limiting the number of repeats with `take`.

  # Examples

  # bad
  (take 5 (repeatedly (range 10))

  # good
  (repeatedly 5 (range 10))
  "
  {:pattern '(take ?n (repeatedly ?coll))
   :message "Rely on the `n` arity of `repeatedly`."
   :replace '(repeatedly ?n ?coll)})