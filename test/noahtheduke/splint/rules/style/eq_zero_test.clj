; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.eq-zero-test
  (:require
   [lazytest.core :refer [defdescribe it describe]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'style/eq-zero)

(defdescribe eq-0-test
  (describe =
    (it "checks 0 first"
      (expect-match
        [{:rule-name rule-name
          :form '(= 0 x)
          :alt '(zero? x)}]
        "(= 0 x)"
        (single-rule-config rule-name)))

    (it "checks 0 second"
      (expect-match
        [{:alt '(zero? x)}]
        "(= x 0)"
        (single-rule-config rule-name))))

  (describe ==
    (it "checks 0 first"
      (expect-match
        [{:alt '(zero? x)}]
        "(== 0 x)"
        (single-rule-config rule-name)))

    (it "checks 0 second"
      (expect-match
        [{:alt '(zero? x)}]
        "(== x 0)"
        (single-rule-config rule-name)))))
