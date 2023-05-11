; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.cond-else-test
  (:require
    [expectations.clojure.test :refer [defexpect]]
    [noahtheduke.splint.test-helpers :refer [expect-match]]))

(defexpect cond-else-test
  (expect-match
    '[{:alt (cond (pos? x) (inc x) :else -1)}]
    "(cond (pos? x) (inc x) :default -1)")
  (expect-match
    '[{:alt (cond (pos? x) (inc x) :else -1)}]
    "(cond (pos? x) (inc x) true -1)")
  (expect-match nil
    "(cond (pos? x) (inc x) (neg? x) (dec x))")
  (expect-match nil
    "(cond :else true)"))
