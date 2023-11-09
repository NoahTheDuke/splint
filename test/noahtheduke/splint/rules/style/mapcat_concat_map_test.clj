; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.mapcat-concat-map-test
  (:require
    [expectations.clojure.test :refer [defexpect]]
    [noahtheduke.splint.test-helpers :refer [expect-match]]))

(set! *warn-on-reflection* true)

(defexpect mapcat-concat-map-test
  (expect-match
    '[{:alt (mapcat x y)}]
    "(apply concat (map x y))")
  (expect-match
    '[{:alt (mapcat x y z)}]
    "(apply concat (map x y z))"))
