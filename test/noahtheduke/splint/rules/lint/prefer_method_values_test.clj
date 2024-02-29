; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.prefer-method-values-test
  (:require
   [clojure.test :refer [deftest]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(defn config []
  (assoc (single-rule-config 'lint/prefer-method-values)
    :clojure-version {:major 1 :minor 12}))

(deftest prefer-method-values-test
  (expect-match
    [{:rule-name 'lint/prefer-method-values
      :form '(.toUpperCase "noah")
      :message "Prefer uniform Class/member syntax instead of traditional interop."
      :alt nil}]
    "(.toUpperCase \"noah\")"
    (config))
  (expect-match
    [{:rule-name 'lint/prefer-method-values
      :form '(. "noah" toUpperCase)
      :message "Prefer uniform Class/member syntax instead of traditional interop."
      :alt nil}]
    "(. \"noah\" toUpperCase)"
    (config)))

(deftest under-version-test
  (expect-match
    nil
    "(. \"noah\" toUpperCase)"
    (assoc (config) :clojure-version {:major 1 :minor 11})))
