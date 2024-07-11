; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.let-do-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'style/let-do)

(defdescribe let-do-test
  (it "works with one child"
    (expect-match
      [{:rule-name rule-name
        :form '(let [a 1 b 2] (do a b))
        :alt '(let [a 1 b 2] a b)}]
      "(let [a 1 b 2] (do a b))"
      (single-rule-config rule-name)))
  (it "ignores multiple children"
    (expect-match
      nil
      "(let [a 1 b 2] (do a b) (do a b))"
      (single-rule-config rule-name))))
