; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.performance.into-transducer-test
  (:require
   [expectations.clojure.test :refer [defexpect]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]
   [noahtheduke.splint.rules.performance.into-transducer :refer [transducers]]))

(set! *warn-on-reflection* true)

(defn config [] (single-rule-config 'performance/into-transducer))

(defexpect into-transducer-test
  (doseq [t transducers]
    (expect-match
      [{:rule-name 'performance/into-transducer
        :form (list 'into [] (list t 'f '(range 100)))
        :message "Rely on the transducer form."
        :alt (list 'into [] (list t 'f) '(range 100))}]
      (format "(into [] (%s f (range 100)))" t)
      (config)))
  (expect-match
    nil
    "(into [1 2 3] (map f (range 100)))"
    (config))
  (expect-match
    nil
    "(into [] (mapv f (range 100)))"
    (config)))
