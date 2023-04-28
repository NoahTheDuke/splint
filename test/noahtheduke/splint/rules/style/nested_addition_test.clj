; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.nested-addition-test
  (:require
    [expectations.clojure.test :refer [defexpect expect]]
    [noahtheduke.splint.test-helpers :refer [check-alt]]))

(defexpect nested-addition-test
  (expect '(+ x y z) (check-alt "(+ x (+ y z))"))
  (expect '(+ x y z a) (check-alt "(+ x (+ y z a))")))
