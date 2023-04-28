; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.naming.predicate-test
  (:require
    [expectations.clojure.test :refer [defexpect expect]]
    [noahtheduke.splint.test-helpers :refer [check-alt]]))

(defexpect predicate-test
  (expect '(defn palindrome? [a] true)
    (check-alt "(defn is-palindrome [a] true)"))
  (expect '(defn palindrome? [a] true)
    (check-alt "(defn palindrome-p [a] true)"))
  (expect nil? (check-alt "(defn palindrome? [a] true)"))
  (expect '(defn palindrome? [a] true)
    (check-alt "(defn is-palindrome? [a] true)")))
