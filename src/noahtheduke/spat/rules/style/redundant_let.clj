; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.spat.rules.style.redundant-let
  (:require
    [noahtheduke.spat.rules :refer [defrule ->violation]]))

(defrule redundant-let
  "Directly nested lets can be merged into a single let block.

  Examples:

  # bad
  (let [a 1]
    (let [b 2]
      (println a b)))

  (let [a 1
        b 2]
    (println a b))
  "
  {:pattern '(let %vector?%-?outer-bindings (let %vector?%-?inner-bindings &&. ?body))
   :message "Redundant let expressions can be merged."
   :on-match (fn [rule form {:syms [?outer-bindings ?inner-bindings ?body]}]
               (let [new-form (list* 'let
                                     (vec (concat ?outer-bindings ?inner-bindings))
                                     ?body)]
                 (->violation rule form {:replace-form new-form})))})
