; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.lint.incorrectly-swapped
  (:require
   [noahtheduke.splint.diagnostic :refer [->diagnostic]]
   [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defrule lint/incorrectly-swapped
  "It can be necessary to swap two variables. This usually requires an intermediate variable, but with destructuring, Clojure can perform this in a single line. However, without an intermediate variable or destructuring, manually swapping can result in both variables ending up with the same value.

  @examples

  ; avoid
  (let [a b
        b a] ...)

  ; prefer
  (let [[a b] [b a]] ...)
  "
  {:pattern '(let [?*args] ?*_)
   :autocorrect false
   :on-match (fn [ctx rule form {:syms [?args]}]
               (when (even? (count ?args))
                 (for [[[bind1 expr1] [bind2 expr2]] (partition 2 1 (partition 2 ?args))
                       :when (and (= bind1 expr2)
                               (= bind2 expr1))
                       :let [old-form (list bind1 expr1 bind2 expr2)
                             new-form (list [bind1 expr1] [bind2 expr2])
                             message "Looks like an incorrect variable swap."]]
                   (->diagnostic ctx rule old-form {:message message
                                                    :replace-form new-form
                                                    :form-meta (meta bind1)}))))})
