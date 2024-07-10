; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.eq-nil-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'style/eq-nil)

(defn config [& {:as style}]
  (cond-> (single-rule-config rule-name)
    style (update rule-name merge style)))

(defdescribe eq-nil-test
  (it "checks nil first"
    (expect-match
      [{:rule-name rule-name
        :form '(= nil x)
        :alt '(nil? x)}]
      "(= nil x)"
      (config)))
  (it "checks nil second"
    (expect-match
      [{:rule-name rule-name
        :form '(= x nil)
        :alt '(nil? x)}]
      "(= x nil)"
      (config))))
