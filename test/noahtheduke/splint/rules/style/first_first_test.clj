; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.first-first-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'style/first-first)

(defdescribe first-first-test
  (it "works"
    (expect-match
      [{:rule-name rule-name
        :form '(first (first coll))
        :alt '(ffirst coll)}]
      "(first (first coll))"
      (single-rule-config rule-name))))
