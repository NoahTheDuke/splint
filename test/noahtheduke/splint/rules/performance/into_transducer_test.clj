; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.performance.into-transducer-test
  (:require
   [lazytest.core :refer [defdescribe describe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'performance/into-transducer)

(defdescribe into-transducer-test
  (describe "handles built-ins"
    (describe "0-arg"
      (map (fn [t]
             (it t
               (expect-match
                 [{:rule-name 'performance/into-transducer
                   :form (list 'into [] (list t '(range 100)))
                   :message "Rely on the transducer form."
                   :alt (list 'into [] (list t) '(range 100))}]
                 (format "(into [] (%s (range 100)))" t)
                 (single-rule-config rule-name))))
        '[dedupe distinct])
      (it "doesn't trigger on 1-arg transducers"
        (expect-match
          nil
          "(into [] (drop (range 100)))"
          (single-rule-config rule-name))))
    (describe "1 arg"
      (map (fn [t]
             (it t
               (expect-match
                 [{:rule-name 'performance/into-transducer
                   :form (list 'into [] (list t 'f '(range 100)))
                   :message "Rely on the transducer form."
                   :alt (list 'into [] (list t 'f) '(range 100))}]
                 (format "(into [] (%s f (range 100)))" t)
                 (single-rule-config rule-name))))
        '[drop drop-while filter halt-when interpose keep keep-indexed map
          map-indexed mapcat partition-all partition-by random-sample remove
          replace take take-nth take-while])
      (it "doesn't trigger on 0-arg transducers"
        (expect-match
          nil
          "(into [] (dedupe 1 (range 100)))"
          (single-rule-config rule-name)))))
  (describe "can check custom functions"
    (it "handles 0-arg fns"
      (expect-match
        [{:rule-name 'performance/into-transducer
          :form (list 'into [] (list 'cool-fn '(range 100)))
          :message "Rely on the transducer form."
          :alt (list 'into [] (list 'cool-fn) '(range 100))}]
        "(into [] (cool-fn (range 100)))"
        (single-rule-config rule-name {:fn-0-arg ['cool-fn]})))
    (it "handles 1-arg fns"
      (expect-match
        [{:rule-name 'performance/into-transducer
          :form '(into [] (cool-fn f (range 100)))
          :message "Rely on the transducer form."
          :alt '(into [] (cool-fn f) (range 100))}]
        "(into [] (cool-fn f (range 100)))"
        (single-rule-config rule-name {:fn-1-arg ['cool-fn]}))))
  (it "matches non-empty into vectors"
    (expect-match
      [{:rule-name 'performance/into-transducer
        :form '(into [1 2 3] (map f (range 100)))
        :message "Rely on the transducer form."
        :alt '(into [1 2 3] (map f) (range 100))}]
      "(into [1 2 3] (map f (range 100)))"
      (single-rule-config rule-name)))
  (it "ignores multi-arg calls"
    (expect-match
      nil
      "(into [] (map f g h (range 100)))"
      (single-rule-config rule-name)))
  (it "ignores mapv"
    (expect-match
      nil
      "(into [] (mapv f (range 100)))"
      (single-rule-config rule-name))))
