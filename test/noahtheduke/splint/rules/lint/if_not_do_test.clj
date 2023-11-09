; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.if-not-do-test
  (:require
    [expectations.clojure.test :refer [defexpect]]
    [noahtheduke.splint.test-helpers :refer [expect-match]]))

(set! *warn-on-reflection* true)

(defexpect if-not-do-test
  (expect-match
    '[{:alt (when-not x y z)}]
    "(if-not x (do y z))")
  (expect-match
    '[{:alt (when-not x y z)}]
    "(if-not x (do y z) nil)"))
