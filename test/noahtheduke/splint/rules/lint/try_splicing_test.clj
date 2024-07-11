; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.try-splicing-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'lint/try-splicing)

(defdescribe try-splicing-test
  (it "works"
    (expect-match
      [{:rule-name 'lint/try-splicing
        :form '(try (splint/unquote-splicing body) (finally :true))
        :alt '(try (do (splint/unquote-splicing body)) (finally :true))}]
      "(try ~@body (finally :true))"
      (single-rule-config rule-name))))
