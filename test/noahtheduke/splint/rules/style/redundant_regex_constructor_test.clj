; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.redundant-regex-constructor-test
  (:require
   [expectations.clojure.test :refer [defexpect expecting]]
   [noahtheduke.splint.test-helpers :refer [expect-match]]))

(set! *warn-on-reflection* true)

(defexpect redundant-regex-constructor-test
  (expect-match
    [{:message "Rely on regex literal directly."
      :alt '(splint/re-pattern "asdf")}]
    "(re-pattern #\"asdf\")")
  (expecting "handles strings"
    (expect-match
      [{:message "Rely on regex literal directly."
        :alt '(splint/re-pattern "asdf")}]
      "(re-pattern \"asdf\")"))
  (expecting "compiles strings into regex"
    (expect-match
      [{:message "Rely on regex literal directly."
        :alt '(splint/re-pattern "\\asdf")}]
      "(re-pattern \"\\\\asdf\")")))
