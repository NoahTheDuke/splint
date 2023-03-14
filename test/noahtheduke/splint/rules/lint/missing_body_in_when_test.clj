; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.missing-body-in-when-test
  (:require
    [expectations.clojure.test :refer [defexpect expect]]
    [noahtheduke.splint-test :refer [check-str]]))

(defexpect missing-body-in-when-test
  (expect "Missing body in when"
    (:message (first (check-str "(when true)"))))
  (expect "Missing body in when"
    (:message (first (check-str "(when (some-func))")))))
