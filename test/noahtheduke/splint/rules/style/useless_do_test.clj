; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.useless-do-test
  (:require
    [expectations.clojure.test :refer [defexpect]]
    [noahtheduke.splint.test-helpers :refer [expect-match]]))

(defexpect useless-do-x-test
  (expect-match
    '[{:alt x}]
    "(do x)")
  (expect-match nil "#(do [%1 %2])")
  (expect-match nil "(do ~@body)"))
