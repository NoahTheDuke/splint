; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.if-same-truthy-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'lint/if-same-truthy)

(defdescribe if-x-x-y-test
  (it "respects 3 arity"
    (expect-match
      [{:rule-name 'lint/if-same-truthy
        :form '(if x x y)
        :alt '(or x y)}]
      "(if x x y)"
      (single-rule-config rule-name)))
  (it "ignores when no match"
    (expect-match nil
      "(if false (reset! state true) (go a))"
      (single-rule-config rule-name))))
