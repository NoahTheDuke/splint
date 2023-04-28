; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.prefer-clj-math-test
  (:require
    [expectations.clojure.test :refer [defexpect expect]]
    [noahtheduke.splint.test-helpers :refer [check-all check-alt]]))

(defexpect prefer-clj-math-test
  (expect '[clojure.math/atan] (map :alt (check-all "(Math/atan 45)")))
  (expect 'clojure.math/PI (check-alt "Math/PI")))
