; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.pos-checks-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'style/pos-checks)

(defdescribe lt-0-x-test
  (it "works with less than"
    (expect-match
      [{:rule-name rule-name
        :form '(< 0 x)
        :alt '(pos? x)}]
      "(< 0 x)"
      (single-rule-config rule-name)))

  (it "works with greater than"
    (expect-match
      [{:rule-name rule-name
        :form '(> x 0)
        :alt '(pos? x)}]
      "(> x 0)"
      (single-rule-config rule-name))))
