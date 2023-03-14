; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.prefer-boolean-test
  (:require
    [expectations.clojure.test :refer [defexpect expect]]
    [noahtheduke.splint-test :refer [check-alt]]))

(defexpect prefer-boolean-test
  (expect '(boolean some-val)
    (check-alt "(if some-val true false)"))
  (expect '(boolean (some-func a b c))
    (check-alt "(if (some-func a b c) true false)")))
