; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.rand-int-one-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'lint/rand-int-one)

(defdescribe rand-int-one-test
  (it "works on integers"
    (expect-match
      [{:rule-name rule-name
        :form '(rand-int 1)
        :message "Always returns 0. Did you mean (rand 1) or (rand-int 2)?"
        :alt nil}]
      "(rand-int 1)"
      (single-rule-config rule-name))
    (expect-match
      [{:rule-name rule-name
        :form '(rand-int -1)
        :message "Always returns 0. Did you mean (rand -1) or (rand-int 2)?"
        :alt nil}]
      "(rand-int -1)"
      (single-rule-config rule-name)))
  (it "works on floats"
    (expect-match
      [{:rule-name rule-name
        :form '(rand-int 1.0)
        :message "Always returns 0. Did you mean (rand 1.0) or (rand-int 2)?"
        :alt nil}]
      "(rand-int 1.0)"
      (single-rule-config rule-name))
    (expect-match
      [{:rule-name rule-name
        :form '(rand-int 1.5)
        :message "Always returns 0. Did you mean (rand 1.5) or (rand-int 2)?"
        :alt nil}]
      "(rand-int 1.5)"
      (single-rule-config rule-name))))
