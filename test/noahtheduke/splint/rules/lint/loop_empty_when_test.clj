; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.loop-empty-when-test
  (:require
   [expectations.clojure.test :refer [defexpect]]
   [noahtheduke.splint.test-helpers :refer [expect-match]]))

(set! *warn-on-reflection* true)

(defexpect loop-empty-when-test
  (expect-match
    '[{:alt (while (= 1 1) (prn 1) (prn 2))}]
    "(loop [] (when (= 1 1) (prn 1) (prn 2) (recur)))"))
