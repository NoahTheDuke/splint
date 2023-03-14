; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.lint.fn-wrapper
  (:require
    [noahtheduke.splint.rules :refer [defrule]]))

(defrule lint/fn-wrapper
  "Avoid wrapping functions in pass-through anonymous function defitions.

  Examples:

  ; bad
  (fn [num] (even? num))

  ; good
  even?

  ; bad
  (let [f (fn [num] (even? num))] ...)

  ; good
  (let [f even?] ...)
  "
  {:patterns ['(%fn?? [?arg] (?fun ?arg))
              '(%fn?? ([?arg] (?fun ?arg)))]
   :message "No need to wrap function. Clojure supports first-class functions."
   :replace '?fun})
