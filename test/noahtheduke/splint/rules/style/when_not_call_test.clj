; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.when-not-call-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'style/when-not-call)

(defdescribe when-not-x-y-test
  (it "works"
    (expect-match
      [{:rule-name rule-name
        :form '(when (not x) y)
        :alt '(when-not x y)}]
      "(when (not x) y)"
      (single-rule-config rule-name))))
