; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.metrics.fn-length-test
  (:require
    [expectations.clojure.test :refer [defexpect expect]]
    [noahtheduke.splint.test-helpers :refer [check-all]]
    [matcher-combinators.test :refer [match?]]))

(defexpect fn-length-test
  (expect nil
    (check-all "(defn n\n[]\n1 2 3)"))
  (expect
    (match?
      [{:message "Function bodies shouldn't be longer than 10 lines."
        :line 3
        :column 1}]
      (check-all "(defn n\n([] 1 2 3)\n([arg1]\na\nb\nc\nd\n5\n6\n7\n8\n9\n10\n11))"))))
