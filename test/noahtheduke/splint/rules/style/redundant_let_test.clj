; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.redundant-let-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'style/redundant-let)

(defdescribe redundant-let-test
  (it "works"
    (expect-match
      [{:rule-name 'style/redundant-let
        :form '(let [a 1] (let [b 2] (println a b)))
        :alt '(let [a 1 b 2] (println a b))}]
      "(let [a 1] (let [b 2] (println a b)))"
      (single-rule-config rule-name))))
