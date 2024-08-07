; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.next-next-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'style/next-next)

(defdescribe next-next-test
  (it "works"
    (expect-match
      [{:rule-name rule-name
        :form '(next (next coll))
        :alt '(nnext coll)}]
      "(next (next coll))"
      (single-rule-config rule-name))))
