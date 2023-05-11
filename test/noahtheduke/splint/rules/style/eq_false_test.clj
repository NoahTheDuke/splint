; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.eq-false-test
  (:require
    [expectations.clojure.test :refer [defexpect]]
    [noahtheduke.splint.test-helpers :refer [expect-match]]))

(defexpect eq-false-test
  (expect-match
    '[{:alt (false? x)}]
    "(= false x)")
  (expect-match
    '[{:alt (false? x)}]
    "(= x false)"))
