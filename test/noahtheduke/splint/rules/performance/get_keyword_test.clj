; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.performance.get-keyword-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'performance/get-keyword)

(defn config [& {:as style}]
  (cond-> (single-rule-config rule-name)
    style (update rule-name merge style)))

(defdescribe get-keyword-test
  (it "only looks for keywords"
    (expect-match
      [{:rule-name rule-name
        :form '(get m :some-key)
        :message "Use keywords as functions instead of the polymorphic function `get`."
        :alt '(:some-key m)}]
      "(get m :some-key)"
      (config)))
  (it "ignores symbols"
    (expect-match
      nil
      "(get m 'some-key)"
      (config)))
  (it "ignores strings"
    (expect-match
      nil
      "(get m \"some-key\")"
      (config)))
  (it "ignores calls without 'get'"
    (expect-match
      nil
      "(m :some-key)"
      (config))))
