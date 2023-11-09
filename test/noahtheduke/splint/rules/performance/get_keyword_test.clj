; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.performance.get-keyword-test
  (:require
    [expectations.clojure.test :refer [defexpect]]
    [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(defn config [] (single-rule-config 'performance/get-keyword))

(defexpect get-keyword-test
  (expect-match
    [{:rule-name 'performance/get-keyword
      :form '(get m :some-key)
      :message "Use keywords as functions instead of the polymorphic function `get`."
      :alt '(:some-key m)}]
    "(get m :some-key)"
    (config))
  (expect-match
    nil
    "(get m 'some-key)"
    (config))
  (expect-match
    nil
    "(m :some-key)"
    (config))
  (expect-match
    nil
    "(get m \"some-key\")"
    (config)))
