; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.no-target-for-method-test
  (:require
    [lazytest.core :refer [defdescribe it]]
    [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'lint/no-target-for-method)

(defdescribe no-target-for-method-test
  (it "works"
    (expect-match
      [{:rule-name rule-name
        :form '(.length)
        :message "Instance methods require a target instance."
        :alt nil}]
      "(.length)"
      (single-rule-config rule-name))
    (expect-match
      [{:rule-name rule-name
        :form '(String/.length)
        :message "Instance methods require a target instance."
        :alt nil}]
      "(String/.length)"
      (single-rule-config rule-name)))
  (it "ignores nested calls"
    (expect-match
      nil
      "(doto (new java.util.HashMap) (.put \"a\" 1) (.put \"b\" 2))"
      (single-rule-config rule-name)))
  (it "ignores non-interop calls"
    (expect-match
      nil
      "(foo)"
      (single-rule-config rule-name))))
