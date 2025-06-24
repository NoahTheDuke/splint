; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.no-op-assignment-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'lint/no-op-assignment)

(defdescribe no-op-assignment-test
  (it "handles the trivial case"
    (expect-match
      [{:rule-name rule-name
        :form '(foo foo)
        :message "Avoid no-op assignment."
        :alt nil}]
      "(let [foo foo] bar)"
      (single-rule-config rule-name)))
  (it "checks multiple cases at once"
    (expect-match
      [{:rule-name rule-name
        :form '(bar bar)
        :message "Avoid no-op assignment."
        :alt nil}
       {:rule-name rule-name
        :form '(foo foo)
        :message "Avoid no-op assignment."
        :alt nil}]
      "(let [foo 1 foo foo bar foo bar bar] bar)"
      (single-rule-config rule-name)))
  (it "doesn't raise when in a reader conditional"
    (expect-match
      nil
      "(let [remote #?(:clj remote :cljs (foo bar))])"
      (single-rule-config rule-name))))
