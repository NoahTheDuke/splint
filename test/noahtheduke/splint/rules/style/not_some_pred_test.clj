; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.not-some-pred-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'style/not-some-pred)

(defdescribe not-some-pred-test
  (it "works with symbols"
    (expect-match
      [{:rule-name rule-name
        :form '(not (some pred coll))
        :alt '(not-any? pred coll)}]
      "(not (some pred coll))"
      (single-rule-config rule-name)))
  
  (it "works with non-symbols"
    (expect-match
      [{:rule-name rule-name
        :form '(not (some (splint/fn [%1] (even? (+ 1 %1))) coll))
        :alt '(not-any? (splint/fn [%1] (even? (+ 1 %1))) coll)}]
      "(not (some #(even? (+ 1 %)) coll))"
      (single-rule-config rule-name))))
