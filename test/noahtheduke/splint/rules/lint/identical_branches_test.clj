; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.identical-branches-test
  (:require
   [lazytest.core :refer [defdescribe it describe]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'lint/identical-branches)

(defdescribe identical-branches-test
  (describe '(var if)
    (it "handles literals"
      (expect-match
        [{:rule-name rule-name
          :form '(if (pred)
                   1
                   1)
          :message "Both branches are identical"
          :alt nil}]
        "(if (pred) 1 1 )"
        (single-rule-config rule-name)))
    (it "handles collections"
      (expect-match
        [{:rule-name rule-name
          :form '(if (pred)
                   [1 2 3]
                   [1 2 3])
          :message "Both branches are identical"
          :alt nil}]
        "(if (pred) [1 2 3] [1 2 3])"
        (single-rule-config rule-name)))
    (it "handles calls"
      (expect-match
        [{:rule-name rule-name
          :form '(if (pred)
                   (rand)
                   (rand))
          :message "Both branches are identical"
          :alt nil}]
        "(if (pred) (rand) (rand))"
        (single-rule-config rule-name)))
    (it "ignores whitespace differences"
      (expect-match
        [{:rule-name rule-name
          :form '(if (pred)
                   [1 2 3]
                   [1 2 3])
          :message "Both branches are identical"
          :alt nil}]
        "(if (pred) [1 2 3]\n[1      \n2 \n     3])"
        (single-rule-config rule-name)))
    (it "handles big ints"
      (expect-match
        nil
        "(if (pred) 0 0N)"
        (single-rule-config rule-name))))
  (describe cond
    (it "checks conds"
      (expect-match
        [{:rule-name rule-name
          :form '((pred1) result1 (pred2) result1)
          :message "Two adjacent branches are identical"
          :alt '((or (pred1) (pred2)) result1)}]
        "(cond (pred1) result1 (pred2) result1)"
        (single-rule-config rule-name)))
    (it "only checks consecutive results"
      (expect-match
        nil
        "(cond (pred1) result1 (pred2) other (pred3) result1)"
        (single-rule-config rule-name)))))
