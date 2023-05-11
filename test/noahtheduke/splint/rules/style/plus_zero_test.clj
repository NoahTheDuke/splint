; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.plus-zero-test
  (:require
    [expectations.clojure.test :refer [defexpect]]
    [noahtheduke.splint.test-helpers :refer [expect-match]]))

(defexpect plus-x-0-test
  (expect-match
    '[{:alt x}]
    "(+ x 0)")
  (expect-match
    '[{:alt x}]
    "(+ 0 x)"))
