; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.trivial-for-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'style/trivial-for)

(defn config [& {:as style}]
  (cond-> (single-rule-config rule-name)
    style (update rule-name merge style)))

(defdescribe trivial-for-test
  (it "works"
    (expect-match
      [{:rule-name rule-name
        :form '(for [item items] (f item))
        :message "Avoid trivial usage of `for`."
        :alt '(map f items)}]
      "(for [item items] (f item))"
      (config)))
  (it "ignores multi-arity f calls"
    (expect-match
      nil
      "(for [item items] (f item other-item))"
      (config))))
