; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.performance.get-in-literals-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'performance/get-in-literals)

(defn config [& {:as style}]
  (cond-> (single-rule-config rule-name)
    style (update rule-name merge style)))

(defdescribe get-in-literals-test
  (it "requires only keywords"
    (expect-match
      [{:rule-name 'performance/get-in-literals
        :form '(get-in m [:some-key1 :some-key2 :some-key3])
        :message "Use keywords as functions instead of `get-in`."
        :alt '(-> m :some-key1 :some-key2 :some-key3)}]
      "(get-in m [:some-key1 :some-key2 :some-key3])"
      (config))
    (expect-match
      nil
      "(get-in m [:some-key1 :some-key2 'some-key3])"
      (config))
    (expect-match
      nil
      "(get-in m [:some-key1 some-key2 :some-key3])"
      (config))
    (expect-match
      nil
      "(get-in m [])"
      (config))))
