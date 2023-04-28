; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.naming.single-segment-namespace-test
  (:require
    [expectations.clojure.test :refer [defexpect expect]]
    [noahtheduke.splint.test-helpers :refer [check-alt check-message]]))

(defexpect single-segment-namespace-test
  (expect nil? (check-alt "(ns simple)"))
  (expect nil? (check-alt "(ns foo.bar)"))
  (expect "simple is a single segment. Consider adding an additional segment."
    (check-message "(ns simple)"))
  (expect nil? (check-message "(ns foo.bar)")))

(defexpect single-segment-namespace-special-exceptions-test
  (expect nil? (check-message "(ns build)"))
  (expect nil? (check-message "(ns user)")))
