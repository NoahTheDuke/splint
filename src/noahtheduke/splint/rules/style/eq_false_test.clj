; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.eq-false-test
  (:require
    [expectations.clojure.test :refer [defexpect expect]]
    [noahtheduke.splint-test :refer [check-alt]]))

(defexpect eq-false-test
  (expect '(false? x) (check-alt "(= false x)"))
  (expect '(false? x) (check-alt "(= x false)")))
