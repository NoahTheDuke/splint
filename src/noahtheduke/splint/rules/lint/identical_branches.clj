; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.lint.identical-branches
  (:require
   [noahtheduke.splint.rules :refer [defrule]]
   [noahtheduke.splint.diagnostic :refer [->diagnostic]]))

(set! *warn-on-reflection* true)

(defrule lint/identical-branches
  "Returning branches of an `if` or `cond` should not be identical. There's likely a bug in one of the branches. In `cond` branches, only checks consecutive branches as order of checks might be important otherwise.

  @examples

  ; avoid
  (if (pred)
    [1 2 3]
    [1 2 3])

  (cond
    (pred1) [1 2 3]
    (pred2) [1 2 3]
    (other) [4 5 6])

  ; prefers
  (cond
    (or (pred1) (pred2)) [1 2 3]
    (other) [4 5 6])

  (cond
    (pred1) [1 2 3]
    (other) [4 5 6]
    (pred2) [1 2 3])
  "
  {:patterns ['(if ?_ ?branch ?branch)
              '(cond ?*args)]
   :on-match (fn [ctx rule form {:syms [?branch ?args]}]
               (if ?branch
                 (->diagnostic ctx rule form {:message "Both branches are identical"})
                 (when (even? (count ?args))
                   (let [branches (partition 2 ?args)]
                     (for [[[pred1 result1] [pred2 result2]] (partition 2 1 branches)
                           :when (= result1 result2)]
                       (->diagnostic ctx rule (list pred1 result1 pred2 result2)
                         {:message "Two adjacent branches are identical"
                          :form-meta (or (meta pred1) (meta result1) (meta form))
                          :replace-form (list (list 'or pred1 pred2) result1)}))))))})
