; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.naming.single-segment-namespace-test
  (:require
   [expectations.clojure.test :refer [defexpect]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(defn config [] (single-rule-config 'naming/single-segment-namespace))

(defexpect single-segment-namespace-test
  (expect-match
    [{:form '(ns simple)
      :message "simple is a single segment. Consider adding an additional segment."
      :alt nil}]
    "(ns simple)"
    (config))
  (expect-match nil "(ns foo.bar)" (config)))

(defexpect single-segment-namespace-special-exceptions-test
  (expect-match nil "(ns build)" (config))
  (expect-match nil "(ns user)" (config)))
