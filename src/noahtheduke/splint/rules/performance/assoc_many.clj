; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.performance.assoc-many
  (:require
    [noahtheduke.splint.diagnostic :refer [->diagnostic]]
    [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defrule performance/assoc-many
  "Assoc takes multiple pairs but relies on `seq` stepping. This is slower than
  relying on multiple `assoc` invocations.

  Examples:

  # bad
  (assoc m :k1 1 :k2 2 :k3 3)

  # good
  (-> m
      (assoc :k1 1)
      (assoc :k2 2)
      (assoc :k3 3))
  "
  {:pattern '(assoc ?m ?*keys)
   :message "Faster to call assoc multiple times."
   :on-match (fn [ctx rule form {:syms [?m ?keys]}]
               (let [new-form (apply
                                list '-> ?m
                                (->> ?keys
                                     (partition 2)
                                     (map #(list 'assoc (first %) (second %)))))]
                 (->diagnostic ctx rule form {:replace-form new-form})))})
