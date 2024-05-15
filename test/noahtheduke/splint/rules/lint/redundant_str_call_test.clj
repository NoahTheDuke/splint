; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.redundant-str-call-test
  (:require
   [expectations.clojure.test :refer [defexpect]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(defn config [] (single-rule-config 'lint/redundant-str-call))

(defexpect redundant-str-call-test
  (expect-match
    [{:rule-name 'lint/redundant-str-call
      :form '(str "foo")
      :message "Use the literal directly."
      :alt "foo"}]
    "(str \"foo\")"
    (config))
  (expect-match
    [{:rule-name 'lint/redundant-str-call
      :form '(str (format "foo-%s" some-var))
      :message "`format` unconditionally returns a string."
      :alt '(format "foo-%s" some-var)}]
    "(str (format \"foo-%s\" some-var))"
    (config))
  (expect-match
    [{:rule-name 'lint/redundant-str-call
      :form '(str (str "foo" some-var))
      :message "`str` unconditionally returns a string."
      :alt '(str "foo" some-var)}]
    "(str (str \"foo\" some-var))"
    (config)))
