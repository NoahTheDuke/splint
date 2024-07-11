; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.loop-empty-when-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'lint/loop-empty-when)

(defdescribe loop-empty-when-test
  (it "handles top-level recur"
    (expect-match
      [{:rule-name 'lint/loop-empty-when
        :form '(loop [] (when (= 1 1) (prn 1) (prn 2) (recur)))
        :alt '(while (= 1 1) (prn 1) (prn 2))}]
      "(loop [] (when (= 1 1) (prn 1) (prn 2) (recur)))"
      (single-rule-config rule-name)))
  (it "ignores nested recurs"
    (expect-match
      nil
      "(loop [] (when (= 1 1) (prn 1) (prn 2) (do (recur))))"
      (single-rule-config rule-name))))
