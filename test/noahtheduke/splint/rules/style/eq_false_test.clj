; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.eq-false-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'style/eq-false)

(defdescribe eq-false-test
  (it "checks false first"
    (expect-match
      [{:rule-name rule-name
        :form '(= false x)
        :alt '(false? x)}]
      "(= false x)"
      (single-rule-config rule-name)))
  (it "checks false second"
    (expect-match
      [{:rule-name rule-name
        :form '(= x false)
        :alt '(false? x)}]
      "(= x false)"
      (single-rule-config rule-name))))
