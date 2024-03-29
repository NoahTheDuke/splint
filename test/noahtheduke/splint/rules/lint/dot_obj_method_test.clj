; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.dot-obj-method-test
  (:require
   [clojure.test :refer [deftest]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(defn config [] (single-rule-config 'lint/dot-obj-method))

(deftest dot-obj-usage-test
  (expect-match
    [{:form '(. obj method 1 2 3)
      :message "Intention is clearer with `.method` form."
      :alt '(.method obj 1 2 3)}]
    "(. obj method 1 2 3)"
    (config)))
