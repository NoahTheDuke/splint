; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.performance.into-transducer-test
  (:require
   [lazytest.core :refer [defdescribe it describe]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]
   [noahtheduke.splint.rules.performance.into-transducer :refer [transducers]]))

(set! *warn-on-reflection* true)

(def rule-name 'performance/into-transducer)

(defn config [& {:as style}]
  (cond-> (single-rule-config rule-name)
    style (update rule-name merge style)))

(defdescribe into-transducer-test
  (describe "handles built-ins"
    (map (fn [t]
           (it t
             (expect-match
               [{:rule-name 'performance/into-transducer
                 :form (list 'into [] (list t 'f '(range 100)))
                 :message "Rely on the transducer form."
                 :alt (list 'into [] (list t 'f) '(range 100))}]
               (format "(into [] (%s f (range 100)))" t)
               (config))))
         transducers))
  (it "ignores non-empty into vectors"
    (expect-match
      nil
      "(into [1 2 3] (map f (range 100)))"
      (config)))
  (it "ignores mapv"
    (expect-match
      nil
      "(into [] (mapv f (range 100)))"
      (config))))
