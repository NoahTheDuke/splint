; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.useless-do-test
  (:require
    [clojure.test :refer [deftest]]
    [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(defn config [] (single-rule-config 'style/useless-do))

(deftest useless-do-x-test
  (expect-match
    [{:form '(do x)
      :message "Unnecessary `do`."
      :alt 'x}]
    "(do x)"
    (config))
  (expect-match nil "#(do [%1 %2])" (config))
  (expect-match nil "(do ~@body)" (config)))
