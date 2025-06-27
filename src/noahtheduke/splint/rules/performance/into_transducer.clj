; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.performance.into-transducer
  (:require
   [noahtheduke.splint.rules :refer [defrule]]
   [noahtheduke.splint.diagnostic :refer [->diagnostic]]
   [noahtheduke.splint.clojure-ext.core :refer [->list]]))

(set! *warn-on-reflection* true)

(defrule performance/into-transducer
  "`into` has a 3-arity and a 4-arity form. Both pour the given coll into the new coll but when given a transducer in the 4-arity form, the transducer is efficiently applied in between.

  Current list of transducers this rule checks:
  > `dedupe`, `distinct`, `drop`, `drop-while`, `filter`, `halt-when`, `interpose`, `keep`, `keep-indexed`, `map`, `map-indexed`, `mapcat`, `partition-all`, `partition-by`, `random-sample`, `remove`, `replace`, `take`, `take-nth`, `take-while`

  This list can be expanded with the configurations `:fn-0-arg` or `:fn-1-arg`, depending on how many arguments the targeted transducer takes

  @examples

  ; avoid
  (into [] (map inc (range 100)))

  ; avoid (with `:fn-names [cool-fn]`)
  (into [] (cool-fn inc (range 100)))

  ; prefer
  (into [] (map inc) (range 100))

  ; prefer (with `:fn-names [cool-fn]`)
  (into [] (cool-fn inc) (range 100))
  "
  {:pattern '(into [?*args] (?trans ??f ?coll))
   :message "Rely on the transducer form."
   :autocorrect true
   :on-match (fn [ctx rule form {:syms [?args ?trans ?f ?coll]}]
               (let [zero-args (:fn-0-arg (:config rule))
                     one-args (:fn-1-arg (:config rule))
                     ?trans-name (symbol (name ?trans))
                     init (vec ?args)]
                 (cond
                   (and (contains? zero-args ?trans-name)
                     (empty? ?f))
                   (let [new-form (list 'into init (list ?trans) ?coll)]
                     (->diagnostic ctx rule form {:replace-form new-form}))
                   (and (contains? one-args ?trans-name)
                     (seq ?f))
                   (let [new-form (list 'into init (->list (list* ?trans ?f)) ?coll)]
                     (->diagnostic ctx rule form {:replace-form new-form})))))})
