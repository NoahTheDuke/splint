; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.performance.into-transducer
  (:require
   [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(def transducers
  #{'cat 'dedupe 'distinct 'drop 'drop-while 'filter 'halt-when
    'interpose 'keep 'keep-indexed 'map 'map-indexed 'mapcat
    'partition-all 'partition-by 'random-sample 'remove 'replace 'take
    'take-nth 'take-while})

(defrule performance/into-transducer
  "`into` has a 3-arity and a 4-arity form. Both pour the given coll into the
  new coll but when given a transducer in the 4-arity form, the transducer is
  efficiently applied in between.

  Examples:

  ; bad
  (into [] (map inc (range 100)))

  ; good
  (into [] (map inc) (range 100))
  "
  {:pattern '(into [] ((? trans transducers) ?f ?coll))
   :message "Rely on the transducer form."
   :replace '(into [] (?trans ?f) ?coll)})
