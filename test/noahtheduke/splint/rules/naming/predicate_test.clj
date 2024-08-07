; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.naming.predicate-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'naming/predicate)

(defdescribe predicate-test
  (it "checks for 'is-x'"
    (expect-match
      [{:rule-name rule-name
        :form '(defn is-palindrome [a] true)
        :alt '(defn palindrome? [a] true)}]
      "(defn is-palindrome [a] true)"
      (single-rule-config rule-name)))
  (it "checks for 'x-p'"
    (expect-match
      '[{:alt (defn palindrome? [a] true)}]
      "(defn palindrome-p [a] true)"
      (single-rule-config rule-name)))
  (it "ignores existing ?"
    (expect-match nil "(defn palindrome? [a] true)" (single-rule-config rule-name)))
  (it "trims leading is- when ? exists"
    (expect-match
      '[{:alt (defn palindrome? [a] true)}]
      "(defn is-palindrome? [a] true)"
      (single-rule-config rule-name))))
