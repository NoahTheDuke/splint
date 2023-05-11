; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.into-literal-test
  (:require
    [expectations.clojure.test :refer [defexpect]]
    [noahtheduke.splint.test-helpers :refer [expect-match]]))

(defexpect into-vec-test
  (expect-match
    '[{:alt (vec coll)}]
    "(into [] coll)"))

(defexpect into-set-test
  (expect-match
    '[{:alt (set coll)}]
    "(into #{} coll)"))
