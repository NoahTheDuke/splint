; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.nested-addition-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'style/nested-addition)

(defn config [& {:as style}]
  (cond-> (single-rule-config rule-name)
    style (update rule-name merge style)))

(defdescribe nested-addition-test
  (it "works"
    (expect-match
      [{:alt '(+ x y z)}]
      "(+ x (+ y z))"
      (config))
    (expect-match
      [{:alt '(+ x y z a)}]
      "(+ x (+ y z a))"
      (config))))
