; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.prefer-condp-test
  (:require
    [expectations.clojure.test :refer [defexpect expect]]
    [noahtheduke.splint.test-helpers :refer [check-alt]]))

(defexpect prefer-condp-test
  (expect '(condp = x 1 :one 2 :two 3 :three 4 :four)
    (check-alt "(cond (= 1 x) :one (= 2 x) :two (= 3 x) :three (= 4 x) :four)"))
  (expect '(condp = x 1 :one 2 :two 3 :three :big)
    (check-alt "(cond (= 1 x) :one (= 2 x) :two (= 3 x) :three :else :big)"))
  (expect '(condp apply [2 3] = "eq" < "lt" > "gt")
    (check-alt "(cond (apply = [2 3]) \"eq\" (apply < [2 3]) \"lt\" (apply > [2 3]) \"gt\")")))
