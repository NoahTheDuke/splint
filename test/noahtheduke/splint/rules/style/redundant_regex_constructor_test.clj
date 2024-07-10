; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.redundant-regex-constructor-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'style/redundant-regex-constructor)

(defn config [& {:as style}]
  (cond-> (single-rule-config rule-name)
    style (update rule-name merge style)))

(defdescribe redundant-regex-constructor-test
  (it "works"
    (expect-match
      [{:message "Rely on regex literal directly."
        :alt '(splint/re-pattern "asdf")}]
      "(re-pattern #\"asdf\")"
      (config)))
  (it "handles strings"
    (expect-match
      [{:alt '(splint/re-pattern "asdf")}]
      "(re-pattern \"asdf\")"
      (config)))
  (it "compiles strings into regex"
    (expect-match
      [{:form '(re-pattern "\\asdf")
        :alt '(splint/re-pattern "\\asdf")}]
      "(re-pattern \"\\\\asdf\")"
      (config))))
