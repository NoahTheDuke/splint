; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.performance.dot-equals-test
  (:require
   [expectations.clojure.test :refer [defexpect]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(defn config [] (single-rule-config 'performance/dot-equals))

(defexpect dot-equals-test
  (expect-match
    [{:rule-name 'performance/dot-equals
      :form '(= "foo" bar)
      :message "Rely on `.equals` when comparing against string literals."
      :alt '(.equals "foo" bar)}]
    "(= \"foo\" bar)"
    (config))
  (expect-match
    [{:rule-name 'performance/dot-equals
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

(defexpect prefer-method-values-interaction-test
  (expect-match
    [{:rule-name 'performance/dot-equals
      :form '(= "foo" bar)
      :message "Rely on `String/.equals` when comparing against string literals."
      :alt '(String/.equals "foo" bar)}]
    "(= \"foo\" bar)"
    (-> (config)
      (assoc :clojure-version {:major 1 :minor 12})
      (update 'lint/prefer-method-values assoc :enabled true)))
  (expect-match
    [{:rule-name 'performance/dot-equals
      :form '(= bar "foo")
      :message "Rely on `String/.equals` when comparing against string literals."
      :alt '(String/.equals "foo" bar)}]
    "(= bar \"foo\")"
    (-> (config)
      (assoc :clojure-version {:major 1 :minor 12})
      (update 'lint/prefer-method-values assoc :enabled true))))
