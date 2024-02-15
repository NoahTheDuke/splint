; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.naming.conversion-functions-test
  (:require
    [clojure.test :refer [deftest]]
    [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(defn config [] (single-rule-config 'naming/conversion-functions))

(deftest conversion-functions-test
  (expect-match
    [{:form '(defn f-to-c ...)
      :message "Use `->` instead of `to` in the names of conversion functions."
      :alt '(defn f->c ...)}]
    "(defn f-to-c [a] {:a a})"
    (config))
  (expect-match
    nil
    "(defn expect-f-to-c [a] {:a a})"
    (config)))
