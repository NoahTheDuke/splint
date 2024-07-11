; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.tostring-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'style/tostring)

(defdescribe str-to-string-test
  (it "checks dot syntax"
    (expect-match
      [{:form '(.toString x)
        :message "Use `str` instead of interop."
        :alt '(str x)}]
      "(.toString x)"
      (single-rule-config rule-name)))
  (it "checks method values syntax"
    (expect-match
      [{:form '(String/toString x)
        :message "Use `str` instead of interop."
        :alt '(str x)}]
      "(String/toString x)"
      (single-rule-config rule-name))))
