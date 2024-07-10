; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.filter-vec-filterv-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'style/filter-vec-filterv)

(defn config [& {:as style}]
  (cond-> (single-rule-config rule-name)
    style (update rule-name merge style)))

(defdescribe filter-vec-filterv-test
  (it "looks for filter"
    (expect-match
      [{:alt '(filterv pred coll)}]
      "(vec (filter pred coll))"
      (config)))
  (it "ignores filterv"
    (expect-match
      nil
      "(vec (filterv pred coll))"
      (config))))
