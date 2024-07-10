; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.performance.avoid-satisfies-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'performance/avoid-satisfies)

(defn config [& {:as style}]
  (cond-> (single-rule-config rule-name)
    style (update rule-name merge style)))

(defdescribe avoid-satisfies-test
  (it "works"
    (expect-match
      [{:rule-name rule-name
        :form '(satisfies? Foo :bar)
        :message "Avoid using `satisfies?`."
        :alt nil}]
      "(satisfies? Foo :bar)"
      (config))))
