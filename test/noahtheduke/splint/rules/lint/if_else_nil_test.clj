; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.if-else-nil-test
  (:require
    [expectations.clojure.test :refer [defexpect]]
    [noahtheduke.splint.test-helpers :refer [expect-match]]))

(set! *warn-on-reflection* true)

(defexpect if-else-nil-test
  (expect-match
    '[{:alt (when x y)}]
    "(if x y nil)")
  (expect-match
    '[{:alt (when x y)}
      {:rule-name style/useless-do}]
    "(if x (do y))")
  (expect-match nil "(if x \"y\" \"z\")"))

(defexpect rest-arg-list-test
  (expect-match
    '[{:alt (when x (if y (z a b c) d))}]
    "(if x (if y (z a b c) d))"))
