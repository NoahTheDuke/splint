; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.redundant-call-test
  (:require
   [lazytest.core :refer [defdescribe it describe]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'lint/redundant-call)

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
        (single-rule-config rule-name))))
  (it "ignores multiple arg"
    (expect-match nil
      "(-> a b (merge c))"
      (single-rule-config rule-name))
    (expect-match nil
      "(cond-> a b (merge c))"
      (single-rule-config rule-name))
    (expect-match nil
      "(some-> a b (merge c))"
      (single-rule-config rule-name)))
  (it "ignores case"
    (expect-match nil
      "(case elem (-> ->>) true false)"
      (single-rule-config rule-name)))
  (describe "autocorrect"
    (it "handles max in thread"
      (expect-match nil
        "(->> [a b c] (reduce (fnil + 0 0)) (max 0))"
        (assoc (single-rule-config rule-name) :autocorrect true)))))
