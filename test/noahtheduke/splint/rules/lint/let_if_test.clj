; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.let-if-test
  (:require
    [expectations.clojure.test :refer [defexpect]]
    [noahtheduke.splint.test-helpers :refer [check-alt]]))

(defexpect let-if-test
  '(if-let [result (some-func)] (do-stuff result) (other-stuff))
  (check-alt "(let [result (some-func)] (if result (do-stuff result) (other-stuff)))"))
