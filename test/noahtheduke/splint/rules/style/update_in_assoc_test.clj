; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.update-in-assoc-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'style/update-in-assoc)

(defn config [& {:as style}]
  (cond-> (single-rule-config rule-name)
    style (update rule-name merge style)))

(defdescribe update-in-assoc-test
  (it "works"
    (expect-match
      [{:rule-name rule-name
        :form '(update-in coll ks assoc v)
        :alt '(assoc-in coll ks v)}]
      "(update-in coll ks assoc v)"
      (config))))
