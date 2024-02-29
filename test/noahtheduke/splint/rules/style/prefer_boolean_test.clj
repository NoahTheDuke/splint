; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.prefer-boolean-test
  (:require
   [expectations.clojure.test :refer [defexpect]]
   [noahtheduke.splint.test-helpers :refer [expect-match]]))

(set! *warn-on-reflection* true)

(defexpect prefer-boolean-test
  (expect-match
    '[{:alt (boolean some-val)}]
    "(if some-val true false)")
  (expect-match
    '[{:alt (boolean (some-func a b c))}]
    "(if (some-func a b c) true false)"))
