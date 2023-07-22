; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.dot-class-method-test
  (:require
    [expectations.clojure.test :refer [defexpect]]
    [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(defn config [] (single-rule-config 'lint/dot-class-method))

(defexpect dot-class-usage-test
  (expect-match
    '[{:alt (Obj/method 1 2 3)}]
    "(. Obj method 1 2 3)"
    (config)))
