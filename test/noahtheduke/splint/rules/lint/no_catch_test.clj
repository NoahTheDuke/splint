; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.no-catch-test
  (:require
   [lazytest.core :refer [defdescribe it describe]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'lint/no-catch)

(defdescribe no-catch-test
  (it "works"
    (expect-match
      [{:rule-name rule-name
        :form '(try (foo))
        :message "Missing `catch` or `finally`."}]
      "(try (foo))"
      (single-rule-config rule-name)))
  (describe "handles :accept-finally style"
    (it "works on catch"
      (expect-match
        nil
        "(try (foo) (catch Exception ex (bar)))"
        (single-rule-config rule-name {:chosen-style :accept-finally})))
    (it "works on finally"
      (expect-match
        nil
        "(try (foo) (finally (bar)))"
        (single-rule-config rule-name {:chosen-style :accept-finally}))))
  (describe "handles :only-catch style"
    (it "rejects finally"
      (expect-match
        [{:rule-name rule-name
          :form '(try (foo) (finally (bar)))
          :message "Missing `catch`."}]
        "(try (foo) (finally (bar)))"
        (single-rule-config rule-name {:chosen-style :only-catch})))
    (it "accepts catch"
      (expect-match
        nil
        "(try (foo) (catch Exception ex (bar)))"
        (single-rule-config rule-name {:chosen-style :only-catch})))))
