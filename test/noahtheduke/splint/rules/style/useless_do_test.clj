; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.useless-do-test
  (:require
    [expectations.clojure.test :refer [defexpect expect]]
    [noahtheduke.splint.test-helpers :refer [check-alt]]))

(defexpect useless-do-x-test
  (expect 'x (check-alt "(do x)"))
  (expect nil? (check-alt "#(do [%1 %2])"))
  (expect nil? (check-alt "(do ~@body)")))
