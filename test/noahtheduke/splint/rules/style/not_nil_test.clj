; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.not-nil-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'style/not-nil?)

(defdescribe not-nil?-test
  (it "works"
    (expect-match
      [{:rule-name rule-name
        :form '(not (nil? x))
        :alt '(some? x)}]
      "(not (nil? x))"
      (single-rule-config rule-name)))
  (it "ignores plain nil"
    (expect-match
      nil
      "(not nil)"
      (single-rule-config rule-name))))
