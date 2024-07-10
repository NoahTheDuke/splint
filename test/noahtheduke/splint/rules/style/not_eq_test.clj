; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.not-eq-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'style/not-eq)

(defn config [& {:as style}]
  (cond-> (single-rule-config rule-name)
    style (update rule-name merge style)))

(defdescribe not-eq-test
  (it "works"
    (expect-match
      [{:rule-name rule-name
        :form '(not (= arg1 arg2 arg3))
        :alt '(not= arg1 arg2 arg3)}]
      "(not (= arg1 arg2 arg3))"
      (config))))
