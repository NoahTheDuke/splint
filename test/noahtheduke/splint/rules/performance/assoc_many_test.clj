; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.performance.assoc-many-test
  (:require
    [expectations.clojure.test :refer [defexpect]]
    [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(defn config [] (single-rule-config 'performance/assoc-many))

(defexpect assoc-many-test
  (expect-match
    [{:rule-name 'performance/assoc-many
      :form '(assoc m :k1 1 :k2 2 :k3 3)
      :message "Faster to call assoc multiple times."
      :alt '(-> m
                (assoc :k1 1)
                (assoc :k2 2)
                (assoc :k3 3))}]
    "(assoc m :k1 1 :k2 2 :k3 3)"
    (config)))
