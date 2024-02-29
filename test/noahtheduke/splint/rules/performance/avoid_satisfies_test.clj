; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.performance.avoid-satisfies-test
  (:require
   [expectations.clojure.test :refer [defexpect]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(defn config [] (single-rule-config 'performance/avoid-satisfies))

(defexpect avoid-satisfies-test
  (expect-match
    [{:rule-name 'performance/avoid-satisfies
      :form '(satisfies? Foo :bar)
      :message "Avoid using `satisfies?`."
      :alt nil}]
    "(satisfies? Foo :bar)"
    (config)))
