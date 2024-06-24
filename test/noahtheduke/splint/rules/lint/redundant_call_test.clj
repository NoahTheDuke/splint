; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.redundant-call-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(defn config [] (single-rule-config 'lint/redundant-call))

(defdescribe redundant-call-test
  (it "handles specific core functions"
    (doseq [given ['(-> x) '(->> x)
                   '(cond-> x) '(cond->> x)
                   '(some-> x) '(some->> x)
                   '(comp x) '(partial x) '(merge x)
                   '(min x) '(max x) '(distinct? x)]]
      (expect-match
        [{:rule-name 'lint/redundant-call
          :form given
          :alt 'x}]
        (str given)
        (config))))
  (it "ignores multiple arg"
    (expect-match nil
      "(-> a b (merge c))"
      (config)))
  (it "ignores case"
    (expect-match nil
      "(case elem (-> ->>) true false)"
      (config))))
