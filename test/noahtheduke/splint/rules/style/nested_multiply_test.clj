; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.nested-multiply-test
  (:require
   [expectations.clojure.test :refer [defexpect]]
   [noahtheduke.splint.test-helpers :refer [expect-match]]))

(set! *warn-on-reflection* true)

(defexpect nested-addition-test
  (expect-match
    '[{:alt (* x y z)}]
    "(* x (* y z))")
  (expect-match
    '[{:alt (* x y z a)}]
    "(* x (* y z a))"))
