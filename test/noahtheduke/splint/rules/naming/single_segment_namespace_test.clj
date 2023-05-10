; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.naming.single-segment-namespace-test
  (:require
    [expectations.clojure.test :refer [defexpect]]
    [noahtheduke.splint.test-helpers :refer [expect-match]]))

(defexpect single-segment-namespace-test
  (expect-match
    '[{:alt nil
       :message "simple is a single segment. Consider adding an additional segment."}]
    "(ns simple)")
  (expect-match nil "(ns foo.bar)"))

(defexpect single-segment-namespace-special-exceptions-test
  (expect-match nil "(ns build)")
  (expect-match nil "(ns user)"))
