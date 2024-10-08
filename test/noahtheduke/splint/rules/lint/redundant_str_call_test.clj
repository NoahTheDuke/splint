; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.redundant-str-call-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'lint/redundant-str-call)

(defdescribe redundant-str-call-test
  (it "works with string literal"
    (expect-match
      [{:rule-name 'lint/redundant-str-call
        :form '(str "foo")
        :message "Use the literal directly."
        :alt "foo"}]
      "(str \"foo\")"
      (single-rule-config rule-name)))
  (it "works with format"
    (expect-match
      [{:rule-name 'lint/redundant-str-call
        :form '(str (format "foo-%s" some-var))
        :message "`format` unconditionally returns a string."
        :alt '(format "foo-%s" some-var)}]
      "(str (format \"foo-%s\" some-var))"
      (single-rule-config rule-name)))
  (it "works with nested strs"
    (expect-match
      [{:rule-name 'lint/redundant-str-call
        :form '(str (str "foo" some-var))
        :message "`str` unconditionally returns a string."
        :alt '(str "foo" some-var)}]
      "(str (str \"foo\" some-var))"
      (single-rule-config rule-name)))
  (it "doesn't do any other checking"
    (expect-match nil
      "(str (str/join \newline (range 10)))"
      (single-rule-config rule-name)))
  (it "checks for threading macros"
    (expect-match nil
      "(-> \"foo\" (str \"bar\"))"
      (single-rule-config rule-name))
    (expect-match nil
      "(->> \"foo\" (str \"bar\"))"
      (single-rule-config rule-name))))
