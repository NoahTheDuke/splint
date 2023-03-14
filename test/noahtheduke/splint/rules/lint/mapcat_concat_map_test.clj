; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.mapcat-concat-map-test
  (:require
    [expectations.clojure.test :refer [defexpect expect]]
    [noahtheduke.splint-test :refer [check-alt]]))

(defexpect mapcat-concat-map-test
  (expect '(mapcat x y) (check-alt "(apply concat (map x y))"))
  (expect '(mapcat x y z) (check-alt "(apply concat (map x y z))")))
