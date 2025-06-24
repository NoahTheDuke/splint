; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.incorrectly-swapped-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'lint/incorrectly-swapped)

(defdescribe incorrectly-swapped-test
  (it "works"
    (expect-match
      [{:rule-name rule-name
        :form '(a b b a)
        :message "Looks like an incorrect variable swap."
        :alt '([a b] [b a])}]
      "(let [a 1 b 2 a b b a] ...)"
      (single-rule-config rule-name)))
  (it "ignores if the lines aren't adjacent"
    (expect-match
      nil
      "(let [a 1 b 2 a b c 10 b a] ...)"
      (single-rule-config rule-name))))
