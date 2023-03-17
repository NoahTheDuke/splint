; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.style.set-literal-as-fn
  (:require
    [noahtheduke.spat.pattern :refer [simple-type drop-quote simple?]]
    [noahtheduke.splint.diagnostic :refer [->diagnostic]]
    [noahtheduke.splint.rules :refer [defrule]]))

(defrule style/set-literal-as-fn
  "Sets can be used as functions and they're converted to static items when
  they contain constants, making them fairly fast. However, they're not as fast
  as [[case]] and their meaning is less clear at first glance.

  Examples:

  # bad
  (#{'a 'b 'c} elem)

  # good
  (case elem (a b c) elem nil)
  "
  {:pattern '(%set?%-?sfn ?elem)
   :message "Prefer `case` to set literal with constant members."
   :on-match (fn [rule form {:syms [?sfn ?elem]}]
               (let [?sfn (map drop-quote ?sfn)]
                 (when (every? #(simple? (simple-type %)) ?sfn)
                   (let [new-form (list 'case ?elem (apply list (sort-by str ?sfn)) ?elem nil)]
                     (->diagnostic rule form {:replace-form new-form})))))})
