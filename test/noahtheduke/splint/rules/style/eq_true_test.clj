; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.eq-true-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'style/eq-true)

(defn config [& {:as style}]
  (cond-> (single-rule-config rule-name)
    style (update rule-name merge style)))

(defdescribe eq-true-test
  (it "checks true first"
    (expect-match
      [{:rule-name rule-name
        :form '(= true x)
        :alt '(true? x)}]
      "(= true x)"
      (config)))
  (it "checks true second"
    (expect-match
      [{:rule-name rule-name
        :form '(= x true)
        :alt '(true? x)}]
      "(= x true)"
      (config))))
