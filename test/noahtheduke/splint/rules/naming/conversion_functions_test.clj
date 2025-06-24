; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.naming.conversion-functions-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'naming/conversion-functions)

(defdescribe conversion-functions-test
  (it "expects single words"
    (expect-match
      [{:form '(defn f-to-c ...)
        :message "Use `->` instead of `to` in the names of conversion functions."
        :alt '(defn f->c ...)}]
      "(defn f-to-c [a] {:a a})"
      (single-rule-config rule-name)))
  (it "rejects multi-words"
    (expect-match
      nil
      "(defn expect-f-to-c [a] {:a a})"
      (single-rule-config rule-name))
    (expect-match
      nil
      "(defn expect-f-to-c-something [a] {:a a})"
      (single-rule-config rule-name))))
