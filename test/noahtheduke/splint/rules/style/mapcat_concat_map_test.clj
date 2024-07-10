; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.mapcat-concat-map-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'style/mapcat-concat-map)

(defn config [& {:as style}]
  (cond-> (single-rule-config rule-name)
    style (update rule-name merge style)))

(defdescribe mapcat-concat-map-test
  (it "works with multiple map arities"
    (expect-match
      [{:rule-name rule-name
        :form '(apply concat (map x y))
        :alt '(mapcat x y)}]
      "(apply concat (map x y))"
      (config))
    (expect-match
      [{:rule-name rule-name
        :form '(apply concat (map x y z))
        :alt '(mapcat x y z)}]
      "(apply concat (map x y z))"
      (config))))
