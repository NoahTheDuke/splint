; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.invalid-regex-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'lint/invalid-regex)

(defdescribe invalid-regex-test
  (it "catches invalid regexes"
    (expect-match
      [{:rule-name rule-name
        :form '(splint/re-pattern "[")
        :message "Invalid java.util.regex.Pattern"}]
      "(re-find #\"[\" s)"
      (single-rule-config rule-name)))
  (it "ignores invalid regexes in cljs"
    (expect-match
      nil
      "(re-find #\"[\" s)"
      (assoc (single-rule-config rule-name) :lang "cljs")))
  (it "ignores valid regexes"
    (expect-match
      nil
      "(re-find #\"[a-z]\" s)"
      (single-rule-config rule-name))))
