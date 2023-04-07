; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.neg-checks-test
  (:require
    [expectations.clojure.test :refer [defexpect]]
    [noahtheduke.splint-test :refer [check-alt]]))

(defexpect lt-x-0-test
  '(neg? x)
  (check-alt "(< x 0)"))

(defexpect gt-0-x-test
  '(neg? x)
  (check-alt "(> 0 x)"))
