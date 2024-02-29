; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.naming.predicate-test
  (:require
   [clojure.test :refer [deftest]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(defn config [] (single-rule-config 'naming/predicate))

(deftest predicate-test
  (expect-match
    [{:form '(defn is-palindrome [a] true)
      :alt '(defn palindrome? [a] true)}]
    "(defn is-palindrome [a] true)"
    (config))
  (expect-match
    '[{:alt (defn palindrome? [a] true)}]
    "(defn palindrome-p [a] true)"
    (config))
  (expect-match nil "(defn palindrome? [a] true)" (config))
  (expect-match
    '[{:alt (defn palindrome? [a] true)}]
    "(defn is-palindrome? [a] true)"
    (config)))
