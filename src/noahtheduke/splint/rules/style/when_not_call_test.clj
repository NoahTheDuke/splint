; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.when-not-call-test
  (:require
    [expectations.clojure.test :refer [defexpect]]
    [noahtheduke.splint-test :refer [check-alt]]))

(defexpect when-not-x-y-test
  '(when-not x y)
  (check-alt "(when (not x) y)"))
