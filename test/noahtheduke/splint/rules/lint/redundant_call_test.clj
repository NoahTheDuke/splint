; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.redundant-call-test
  (:require
    [expectations.clojure.test :refer [defexpect expect from-each]]
    [noahtheduke.splint.test-helpers :refer [check-all check-alt]]))

(defexpect redundant-call-test
  (expect 'x
    (from-each [given ["(-> x)" "(->> x)"
                       "(cond-> x)" "(cond->> x)"
                       "(some-> x)" "(some->> x)"
                       "(comp x)" "(partial x)" "(merge x)"]]
      (check-alt given)))
  (expect nil? (check-all "(-> a b (merge c))"))
  (expect nil? (check-all "(case elem (-> ->>) true false)")))
