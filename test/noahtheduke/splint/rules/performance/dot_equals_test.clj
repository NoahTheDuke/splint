; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.performance.dot-equals-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'performance/dot-equals)

(defn config [& {:as style}]
  (cond-> (single-rule-config rule-name)
    style (update rule-name merge style)))

(defdescribe dot-equals-test
  (it "without lint/prefer-method-values"
    (expect-match
      [{:rule-name rule-name
        :form '(= "foo" bar)
        :message "Rely on `.equals` when comparing against string literals."
        :alt '(.equals "foo" bar)}]
      "(= \"foo\" bar)"
      (config))
    (expect-match
      [{:rule-name rule-name
        :form '(= bar "foo")
        :message "Rely on `.equals` when comparing against string literals."
        :alt '(.equals "foo" bar)}]
      "(= bar \"foo\")"
      (config))
    (expect-match
      nil
      "(= bar foo)"
      (config))
    (expect-match
      nil
      "(= foo bar)"
      (config)))

  (it "with lint/prefer-method-values enabled"
    (expect-match
      [{:rule-name rule-name
        :form '(= "foo" bar)
        :message "Rely on `String/.equals` when comparing against string literals."
        :alt '(String/.equals "foo" bar)}]
      "(= \"foo\" bar)"
      (-> (config)
          (assoc :clojure-version {:major 1 :minor 12})
          (update 'lint/prefer-method-values assoc :enabled true)))
    (expect-match
      [{:rule-name rule-name
        :form '(= bar "foo")
        :message "Rely on `String/.equals` when comparing against string literals."
        :alt '(String/.equals "foo" bar)}]
      "(= bar \"foo\")"
      (-> (config)
          (assoc :clojure-version {:major 1 :minor 12})
          (update 'lint/prefer-method-values assoc :enabled true)))))
