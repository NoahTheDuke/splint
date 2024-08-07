; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.filter-complement-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'style/filter-complement)

(defdescribe filter-complement-test
  (it "complement"
    (expect-match
      [{:alt '(remove pred coll)}]
      "(filter (complement pred) coll)"
      (single-rule-config rule-name)))

  (it "anonymous literal"
    (expect-match
      [{:alt '(remove pred coll)}]
      "(filter #(not (pred %)) coll)"
      (single-rule-config rule-name)))

  (it "fn*"
    (expect-match
      [{:alt '(remove pred coll)}]
      "(filter (fn* [x] (not (pred x))) coll)"
      (single-rule-config rule-name)))

  (it "fn"
    (expect-match
      [{:alt '(remove pred coll)}]
      "(filter (fn [x] (not (pred x))) coll)"
      (single-rule-config rule-name))))
