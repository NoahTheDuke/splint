; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.naming.predicate-test
  (:require
    [expectations.clojure.test :refer [defexpect]]
    [noahtheduke.splint.test-helpers :refer [expect-match]]))

(set! *warn-on-reflection* true)

(defexpect predicate-test
  (expect-match
    '[{:alt (defn palindrome? [a] true)}]
    "(defn is-palindrome [a] true)")
  (expect-match
    '[{:alt (defn palindrome? [a] true)}]
    "(defn palindrome-p [a] true)")
  (expect-match nil "(defn palindrome? [a] true)")
  (expect-match
    '[{:alt (defn palindrome? [a] true)}]
    "(defn is-palindrome? [a] true)"))
