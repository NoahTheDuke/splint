; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.loop-empty-when-test
  (:require
    [expectations.clojure.test :refer [defexpect]]
    [noahtheduke.splint.test-helpers :refer [check-alt]]))

(defexpect loop-empty-when-test
  '(while (= 1 1) (prn 1) (prn 2))
  (check-alt "(loop [] (when (= 1 1) (prn 1) (prn 2) (recur)))"))
