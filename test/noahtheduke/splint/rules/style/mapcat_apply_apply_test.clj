; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.mapcat-apply-apply-test
  (:require
    [expectations.clojure.test :refer [defexpect]]
    [noahtheduke.splint.test-helpers :refer [check-alt]]))

(defexpect mapcat-apply-apply-test
  '(mapcat x y)
  (check-alt "(apply concat (apply map x y))"))
