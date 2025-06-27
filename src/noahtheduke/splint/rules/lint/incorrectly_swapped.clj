; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.lint.incorrectly-swapped
  (:require
   [noahtheduke.splint.diagnostic :refer [->diagnostic]]
   [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defrule lint/incorrectly-swapped
  "It can be necessary to swap two variables. This usually requires an intermediate variable, but with destructuring, Clojure can perform this in a single line. If the destructuring is done incorrectly, then the assignment is a no-op, indicating a bug.

  @examples

  ; avoid
  (let [[a b] [a b]] ...)

  ; prefer
  (let [[a b] [b a]] ...)
  "
  {:pattern '(let [?*args] ?*_)
   :on-match (fn [ctx rule form {:syms [?args]}]
               (when (even? (count ?args))
                 (for [[bind expr] (partition 2 ?args)
                       :when (and (vector? bind)
                               (= 2 (count bind))
                               (vector? expr)
                               (= 2 (count expr)))
                       :let [[bind1 expr1] bind
                             [bind2 expr2] expr]
                       :when (and (= bind1 bind2)
                               (= expr1 expr2))
                       :let [old-form (list [bind1 expr1] [bind2 expr2])
                             new-form (list [bind1 expr1] [expr2 bind2])
                             message "Looks like an incorrect variable swap."]]
                   (->diagnostic ctx rule old-form {:message message
                                                    :replace-form new-form
                                                    :form-meta (meta bind1)}))))})
