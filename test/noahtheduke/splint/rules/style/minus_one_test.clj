; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.minus-one-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'style/minus-one)

(defdescribe minus-x-1-test
  (it "expects 1 to be in final position"
    (expect-match
      [{:rule-name rule-name
        :form '(- x 1)
        :alt '(dec x)}]
      "(- x 1)"
      (single-rule-config rule-name)))
  (it "ignores multi-arity minus"
    (expect-match
      nil
      "(- x y x 1)"
      (single-rule-config rule-name))))
