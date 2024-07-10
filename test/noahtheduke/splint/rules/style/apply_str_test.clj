; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.apply-str-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'style/apply-str)

(defn config [& {:as style}]
  (cond-> (single-rule-config rule-name)
    style (update rule-name merge style)))

(defdescribe apply-str-test
  (it "works"
    (expect-match
      [{:rule-name rule-name
        :form '(apply str x)
        :alt '(clojure.string/join x)}]
      "(apply str x)"
      (config)))
  (it "ignores nested reverse"
    (expect-match
      nil
      "(apply str (reverse x))"
      (config)))
  (it "ignores nested interpose"
    (expect-match
      nil
      "(apply str (interpose x))"
      (config))))
