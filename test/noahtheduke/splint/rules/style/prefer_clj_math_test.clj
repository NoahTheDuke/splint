; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.prefer-clj-math-test
  (:require
    [expectations.clojure.test :refer [defexpect]]
    [noahtheduke.splint.test-helpers :refer [expect-match]]))

(defexpect prefer-clj-math-test
  (expect-match '[{:alt clojure.math/atan}] "(Math/atan 45)")
  (expect-match '[{:alt clojure.math/PI}] "Math/PI"))
