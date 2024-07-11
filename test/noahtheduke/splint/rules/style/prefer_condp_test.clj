; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.prefer-condp-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'style/prefer-condp)

(defdescribe prefer-condp-test
  (it "works with no default case"
    (expect-match
      '[{:alt (condp = x 1 :one 2 :two 3 :three 4 :four)}]
      "(cond (= 1 x) :one (= 2 x) :two (= 3 x) :three (= 4 x) :four)"
      (single-rule-config rule-name)))
  (it "works with a default case"
    (expect-match
      '[{:alt (condp = x 1 :one 2 :two 3 :three :big)}]
      "(cond (= 1 x) :one (= 2 x) :two (= 3 x) :three :else :big)"
      (single-rule-config rule-name)))
  (it "can handle complex test cases"
    (expect-match
      '[{:alt (condp apply [2 3] = "eq" < "lt" > "gt")}]
      "(cond (apply = [2 3]) \"eq\" (apply < [2 3]) \"lt\" (apply > [2 3]) \"gt\")"
      (single-rule-config rule-name)))
  (it "ignores built-in macros"
    (expect-match nil "(cond (and a b) true (and c b) false)" (single-rule-config rule-name))
    (expect-match nil "(cond (or a b) true (or c b) false)" (single-rule-config rule-name))))
