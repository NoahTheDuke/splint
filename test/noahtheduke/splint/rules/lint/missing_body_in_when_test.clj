; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.missing-body-in-when-test
  (:require
    [expectations.clojure.test :refer [defexpect]]
    [noahtheduke.splint.test-helpers :refer [expect-match]]))

(set! *warn-on-reflection* true)

(defexpect missing-body-in-when-test
  (expect-match
    '[{:alt nil
       :message "Missing body in when"}]
    "(when true)")
  (expect-match
    '[{:alt nil
       :message "Missing body in when"}]
    "(when (some-func))"))
