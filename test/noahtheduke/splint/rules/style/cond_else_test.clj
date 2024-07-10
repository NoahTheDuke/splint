; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.cond-else-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'style/cond-else)

(defn config [& {:as style}]
  (cond-> (single-rule-config rule-name)
    style (update rule-name merge style)))

(defdescribe cond-else-test
  (it "checks keywords"
    (expect-match
      [{:rule-name rule-name
        :form '(cond (pos? x) (inc x) :default -1)
        :alt '(cond (pos? x) (inc x) :else -1)}]
      "(cond (pos? x) (inc x) :default -1)"
      (config)))
  (it "checks for true"
    (expect-match
      [{:rule-name rule-name
        :form '(cond (pos? x) (inc x) true -1)
        :alt '(cond (pos? x) (inc x) :else -1)}]
      "(cond (pos? x) (inc x) true -1)"
      (config)))
  (it "ignores no default branch"
    (expect-match nil
      "(cond (pos? x) (inc x) (neg? x) (dec x))"
      (config)))

  (it "ignores existing :else"
    (expect-match nil "(cond :else true)" (config))))
