; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.redundant-call-test
  (:require
    [expectations.clojure.test :refer [defexpect]]
    [noahtheduke.splint.test-helpers :refer [expect-match]]))

(set! *warn-on-reflection* true)

(defexpect redundant-call-test
  (doseq [given ["(-> x)" "(->> x)"
                 "(cond-> x)" "(cond->> x)"
                 "(some-> x)" "(some->> x)"
                 "(comp x)" "(partial x)" "(merge x)"
                 "(min x)" "(max x)" "(distinct? x)"]]
    (expect-match '[{:alt x}] given))
  (expect-match nil "(-> a b (merge c))")
  (expect-match nil "(case elem (-> ->>) true false)"))
