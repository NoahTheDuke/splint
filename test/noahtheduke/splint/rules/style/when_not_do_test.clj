; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.when-not-do-test
  (:require
    [expectations.clojure.test :refer [defexpect]]
    [noahtheduke.splint.test-helpers :refer [expect-match]]))

(defexpect when-not-do-test
  (expect-match
    '[{:alt (when-not x y z)}]
    "(when-not x (do y z))")
  (expect-match
    '[{:alt (when-not x y z)}]
    "(when-not x (do y z) nil)"))
