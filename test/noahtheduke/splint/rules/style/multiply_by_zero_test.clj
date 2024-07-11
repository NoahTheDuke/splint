; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.multiply-by-zero-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'style/multiply-by-zero)

(defdescribe multiply-by-1-test
  (it "works in either order"
    (expect-match [{:alt 0}] "(* x 0)" (single-rule-config rule-name))
    (expect-match [{:alt 0}] "(* 0 x)" (single-rule-config rule-name)))
  (it "ignores multi-arity multiply"
    (expect-match nil "(* x y 0)" (single-rule-config rule-name))
    (expect-match nil "(* 0 x y)" (single-rule-config rule-name))))
