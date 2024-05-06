; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.performance.avoid-satisfies
  (:require
   [noahtheduke.splint.diagnostic :refer [->diagnostic]]
   [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defrule performance/avoid-satisfies
  "Avoid use of `satisfies?` as it is extremely slow. Restructure your code to rely on protocols or other polymorphic forms.

  Examples:

  ; avoid
  (satisfies? Foo :bar)
  "
  {:pattern '(satisfies? ?protocol ?obj)
   :on-match (fn [ctx rule form bindings]
               (->diagnostic ctx rule form {:message "Avoid using `satisfies?`."}))})
