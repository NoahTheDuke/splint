; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.lint.min-max
  (:require
   [noahtheduke.splint.diagnostic :refer [->diagnostic]]
   [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defrule lint/min-max
  "Clamping a value between two numbers requires saying at max of the lower number and a min of the higher number. If the min is lower than the max, then the min 

  @examples

  ; avoid
  (min 10 (max 100 foo))
  (max 100 (min 10 foo))

  ; prefer
  (min 100 (max 10 foo))
  "
  {:patterns ['(min (? min number?) (max (? max number?) ?v))
              '(max (? max number?) (min (? min number?) ?v))]
   :on-match (fn [ctx rule form {:syms [?min ?max ?v]}]
               (when (<= ?min ?max)
                 (let [new-form (when (not= ?min ?max)
                                  (list 'min ?max (list 'max ?min ?v)))
                       message (format "Incorrect clamping, will always be %s." ?min)]
                   (->diagnostic ctx rule form {:replace-form new-form
                                                :message message}))))})
