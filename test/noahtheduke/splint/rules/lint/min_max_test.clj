; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.min-max-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'lint/min-max)

(defdescribe min-max-test
  (it "works if min is the outer call"
    (expect-match
      [{:rule-name rule-name
        :form '(min 0 (max 500 foo))
        :message "Incorrect clamping, will always be 0."
        :alt '(min 500 (max 0 foo))}]
      "(min 0 (max 500 foo))"
      (single-rule-config rule-name)))
  (it "works if max is the outer call"
    (expect-match
      [{:rule-name rule-name
        :form '(max 500 (min 0 foo))
        :message "Incorrect clamping, will always be 0."
        :alt '(min 500 (max 0 foo))}]
      "(max 500 (min 0 foo))"
      (single-rule-config rule-name)))
  (it "Checks if the numbers are the same"
    (expect-match
      [{:rule-name rule-name
        :form '(min 10 (max 10 foo))
        :message "Incorrect clamping, will always be 10."
        :alt nil}]
      "(min 10 (max 10 foo))"
      (single-rule-config rule-name)))
  (it "Ignores if written correctly"
    (expect-match
      nil
      "(min 100 (max 10 foo))"
      (single-rule-config rule-name))))
