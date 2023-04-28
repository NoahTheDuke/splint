; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.eq-true-test
  (:require
    [expectations.clojure.test :refer [defexpect expect]]
    [noahtheduke.splint.test-helpers :refer [check-alt]]))

(defexpect eq-true-test
  (expect '(true? x) (check-alt "(= true x)"))
  (expect '(true? x) (check-alt "(= true x)")))
