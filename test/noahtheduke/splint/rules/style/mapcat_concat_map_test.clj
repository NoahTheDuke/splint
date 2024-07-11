; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.mapcat-concat-map-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'style/mapcat-concat-map)

(defdescribe mapcat-concat-map-test
  (it "works with multiple map arities"
    (expect-match
      [{:rule-name rule-name
        :form '(apply concat (map x y))
        :alt '(mapcat x y)}]
      "(apply concat (map x y))"
      (single-rule-config rule-name))
    (expect-match
      [{:rule-name rule-name
        :form '(apply concat (map x y z))
        :alt '(mapcat x y z)}]
      "(apply concat (map x y z))"
      (single-rule-config rule-name))))
