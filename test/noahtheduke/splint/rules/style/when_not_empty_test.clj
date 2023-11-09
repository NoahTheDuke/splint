; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.when-not-empty-test
  (:require
    [expectations.clojure.test :refer [defexpect]]
    [noahtheduke.splint.test-helpers :refer [expect-match]]))

(set! *warn-on-reflection* true)

(defexpect when-not-empty?-test
  (expect-match
    '[{:alt (when (seq x) y)}]
    "(when-not (empty? x) y)")
  (expect-match nil
    "(if (= 1 called-with) \"arg\" \"args\")"))
