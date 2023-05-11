; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.if-same-truthy-test
  (:require
    [expectations.clojure.test :refer [defexpect]]
    [noahtheduke.splint.test-helpers :refer [expect-match]]))

(defexpect if-x-x-y-test
  (expect-match
    '[{:alt (or x y)}]
    "(if x x y)")
  (expect-match nil
    "(if false (reset! state true) (go a))"))
