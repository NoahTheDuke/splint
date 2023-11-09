; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.pos-checks-test
  (:require
    [expectations.clojure.test :refer [defexpect]]
    [noahtheduke.splint.test-helpers :refer [expect-match]]))

(set! *warn-on-reflection* true)

(defexpect lt-0-x-test
  (expect-match
    '[{:alt (pos? x)}]
    "(< 0 x)"))

(defexpect gt-x-0-test
  (expect-match
    '[{:alt (pos? x)}]
    "(> x 0)"))
