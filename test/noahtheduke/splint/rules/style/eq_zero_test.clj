; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.eq-zero-test
  (:require
    [expectations.clojure.test :refer [defexpect]]
    [noahtheduke.splint.test-helpers :refer [expect-match]]))

(set! *warn-on-reflection* true)

(defexpect eq-0-x-test
  (expect-match
    '[{:alt (zero? x)}]
    "(= 0 x)"))

(defexpect eq-x-0-test
  (expect-match
    '[{:alt (zero? x)}]
    "(= x 0)"))

(defexpect eqeq-0-x-test
  (expect-match
    '[{:alt (zero? x)}]
    "(== 0 x)"))

(defexpect eqeq-x-0-test
  (expect-match
    '[{:alt (zero? x)}]
    "(== x 0)"))
