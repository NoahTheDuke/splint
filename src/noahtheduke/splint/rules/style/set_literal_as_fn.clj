; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.style.set-literal-as-fn
  (:require
    [noahtheduke.spat.pattern :refer [simple-type drop-quote]]
    [noahtheduke.splint.diagnostic :refer [->diagnostic]]
    [noahtheduke.splint.rules :refer [defrule]]))

(defn literal-or-quote?
  "Almost everything in spat.pattern/simple? but quoted symbols
  because sets treat symbols as vars.
  #{'a 'b} is good, #{'a b} is bad because b means 'some val'."
  [form]
  (or (case (simple-type form)
        (:nil :boolean :char :number :keyword :string) true
        false)
      (and (seqable? form)
           (= 2 (count form))
           (= 'quote (first form))
           (case (simple-type (second form))
             (:nil :boolean :char :number :keyword :string :symbol) true
             false))))

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
   :on-match (fn [ctx rule form {:syms [?sfn ?elem]}]
               (when (and (not (list? ?elem))
                          (every? literal-or-quote? ?sfn))
                 (let [case-lst (->> ?sfn
                                     (map drop-quote)
                                     (sort-by str)
                                     (apply list))
                       new-form (list 'case ?elem case-lst ?elem nil)]
                   (->diagnostic rule form {:replace-form new-form}))))})
