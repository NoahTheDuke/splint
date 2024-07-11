; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.assoc-assoc-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'style/assoc-assoc)

(defdescribe assoc-assoc-key-coll-test
  (it "respects keyword first"
    (expect-match
      [{:rule-name rule-name
        :form '(assoc coll :k1 (assoc (:k1 coll) :k2 v))
        :alt '(assoc-in coll [:k1 :k2] v)}]
      "(assoc coll :k1 (assoc (:k1 coll) :k2 v))"
      (single-rule-config rule-name)))

  (it "respects nested coll-first"
    (expect-match
      [{:rule-name rule-name
        :form '(assoc coll :k1 (assoc (coll :k1) :k2 v))
        :alt '(assoc-in coll [:k1 :k2] v)}]
      "(assoc coll :k1 (assoc (coll :k1) :k2 v))"
      (single-rule-config rule-name)))

  (it "respects nested get"
    (expect-match
      [{:rule-name rule-name
        :form '(assoc coll :k1 (assoc (get coll :k1) :k2 v))
        :alt '(assoc-in coll [:k1 :k2] v)}]
      "(assoc coll :k1 (assoc (get coll :k1) :k2 v))"
      (single-rule-config rule-name))))
