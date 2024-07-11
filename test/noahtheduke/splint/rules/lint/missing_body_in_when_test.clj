; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.missing-body-in-when-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'lint/missing-body-in-when)

(defdescribe missing-body-in-when-test
  (it "handles symbols"
    (expect-match
      [{:rule-name 'lint/missing-body-in-when
        :form '(when true)
        :alt nil
        :message "Missing body in when"}]
      "(when true)"
      (single-rule-config rule-name)))
  (it "handles expressions"
    (expect-match
      [{:rule-name 'lint/missing-body-in-when
        :form '(when (some-func))
        :alt nil
        :message "Missing body in when"}]
      "(when (some-func))"
      (single-rule-config rule-name))))
