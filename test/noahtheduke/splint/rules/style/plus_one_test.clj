; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.plus-one-test
  (:require
    [expectations.clojure.test :refer [defexpect]]
    [noahtheduke.splint.test-helpers :refer [expect-match]]))

(defexpect plus-x-1-test
  (expect-match
    '[{:alt (inc x)}]
    "(+ x 1)")
  (expect-match
    '[{:alt (inc x)}]
    "(+ 1 x)"))
