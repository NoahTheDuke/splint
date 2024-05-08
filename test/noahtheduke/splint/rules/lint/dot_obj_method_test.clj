; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.dot-obj-method-test
  (:require
   [expectations.clojure.test :refer [defexpect]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(defn config [] (single-rule-config 'lint/dot-obj-method))

(defexpect dot-obj-usage-test
  (expect-match
    [{:rule-name 'lint/dot-obj-method
      :form '(. obj method 1 2 3)
      :message "Intention is clearer with `.method` form."
      :alt '(.method obj 1 2 3)}]
    "(. obj method 1 2 3)"
    (config))
  (expect-match
    [{:rule-name 'lint/prefer-method-values
      :alt '(CLASS/.method obj 1 2 3)}]
    "(. obj method 1 2 3)"
    (-> (config)
      (assoc :clojure-version {:major 1 :minor 12})
      (assoc-in ['lint/prefer-method-values :enabled] true))))
