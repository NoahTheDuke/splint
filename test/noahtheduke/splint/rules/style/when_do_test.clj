; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.when-do-test
  (:require
   [expectations.clojure.test :refer [defexpect]]
   [noahtheduke.splint.test-helpers :refer [expect-match]]))

(set! *warn-on-reflection* true)

(defexpect when-do-test
  (expect-match
    '[{:alt (when x y)} {:rule-name style/useless-do}]
    "(when x (do y))")
  (expect-match
    '[{:alt (when x y z)}]
    "(when x (do y z))"))
