; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.catch-throwable-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'lint/catch-throwable)

(defdescribe catch-throwable-test
  (it "looks for Throwable"
    (expect-match
      [{:rule-name rule-name
        :form '(catch Throwable t (bar))
        :message "Throwable is too broad to safely catch."
        :alt nil}]
      "(try (foo) (catch Throwable t (bar)))"
      (single-rule-config rule-name)))
  (it "doesn't look for Error with :throwable"
    (expect-match
      nil
      "(try (foo) (catch Error t (bar)))"
      (single-rule-config rule-name)))
  (it "looks for Throwable with custom :throwables"
    (expect-match
      [{:rule-name rule-name
        :form '(catch Throwable t (bar))
        :message "Throwable is too broad to safely catch."
        :alt nil}]
      "(try (foo) (catch Throwable t (bar)))"
      (single-rule-config rule-name {:throwables ['Error]})))
  (it "looks for Error with custom :throwables"
    (expect-match
      [{:rule-name rule-name
        :form '(catch Error t (bar))
        :message "Error is too broad to safely catch."
        :alt nil}]
      "(try (foo) (catch Error t (bar)))"
      (single-rule-config rule-name {:throwables ['Error]})))
  (it "can be as specific as desired"
    (expect-match
      [{:rule-name rule-name
        :form '(catch ExceptionInfo t (bar))
        :message "Catching ExceptionInfo is disallowed."
        :alt nil}]
      "(ns foo (:import clojure.lang.ExceptionInfo))
      (try (foo) (catch ExceptionInfo t (bar)))"
      (single-rule-config rule-name {:throwables ['clojure.lang.ExceptionInfo]}))))
