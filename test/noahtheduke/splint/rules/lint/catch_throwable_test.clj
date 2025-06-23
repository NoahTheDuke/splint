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
  (it "looks for literal Throwable"
    (expect-match
      [{:rule-name rule-name
        :form '(catch Throwable t (bar))
        :message "Throwable is too broad to safely catch."
        :alt '(catch Exception t (bar))}]
      "(try (foo) (catch Throwable t (bar)))"
      (single-rule-config rule-name))))
