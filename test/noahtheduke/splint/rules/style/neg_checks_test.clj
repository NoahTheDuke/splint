; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.neg-checks-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'style/neg-checks)

(defdescribe lt-x-0-test
  (it "recognizes < (less than)"
    (expect-match
      [{:rule-name rule-name
        :form '(< x 0)
        :alt '(neg? x)}]
      "(< x 0)"
      (single-rule-config rule-name)))

  (it "recognizes > (greater than)"
    (expect-match
      [{:rule-name rule-name
        :form '(> 0 x)
        :alt '(neg? x)}]
      "(> 0 x)"
      (single-rule-config rule-name))))
