; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.duplicate-case-test-test
  (:require
   [expectations.clojure.test :refer [defexpect]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(defn config [] (single-rule-config 'lint/duplicate-case-test))

(defexpect duplicate-case-test-test
  (expect-match
    [{:rule-name 'lint/duplicate-case-test
      :form '(case x :foo :bar :foo :baz)
      :message "Duplicate case test constant: :foo"
      :alt nil
      :line 1
      :column 1
      :end-line 1
      :end-column 29}]
    "(case x :foo :bar :foo :baz)"
    (config))
  (expect-match
    [{:rule-name 'lint/duplicate-case-test
      :form '(case x 'foo :bar 'foo :baz)
      :message "Duplicate case test constant: foo"
      :alt nil}
     {:rule-name 'lint/duplicate-case-test
      :form '(case x 'foo :bar 'foo :baz)
      :message "Duplicate case test constant: quote"
      :alt nil}]
    "(case x 'foo :bar 'foo :baz)"
    (config))
  (expect-match
    [{:form '(case x (foo bar) 1 bar 2 (bar baz) 5)
      :message "Duplicate case test constant: bar"
      :line 1
      :column 28
      :end-line 1
      :end-column 31}
     {:message "Duplicate case test constant: bar"
      :line 1
      :column 21
      :end-line 1
      :end-column 24}]
    "(case x (foo bar) 1 bar 2 (bar baz) 5)"
    (config)))
