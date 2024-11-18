; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.style.redundant-nested-call
  (:require
   [noahtheduke.splint.diagnostic :refer [->diagnostic]]
   [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(def relevant-call?
  #{'* '*' '+ '+'
    'and 'comp 'concat 'every-pred 'lazy-cat
    'max 'merge 'min 'or 'some-fn 'str})

(defrule style/redundant-nested-call
  "Some clojure.core functions and macros take a variable number of args, so there's no need to nest calls.

  > [!NOTE]
  > This can have performance implications in certain hot-loops.

  @examples

  ; avoid
  (+ 1 2 (+ 3 4))
  (comp :foo :bar (comp :qux :ply))

  ; prefer
  (+ 1 2 3 4)
  (comp :foo :bar :qux :ply)
  "
  {:pattern '((? call relevant-call?) ?+args (?call ?+others))
   :on-match (fn [ctx rule form {:syms [?call ?args ?others]}]
               (let [new-form (list* ?call (concat ?args ?others))]
                 (->diagnostic ctx rule form {:message (format "Redundant nested call: `%s`." ?call)
                                              :replace-form new-form})))})
