; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.if-let-else-nil-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'lint/if-let-else-nil)

(defdescribe if-let-else-nil-test
  (it "handles no else"
    (expect-match
      [{:rule-name 'lint/if-let-else-nil
        :form '(if-let binding expr)
        :alt '(when-let binding expr)}]
      "(if-let binding expr)"
      (single-rule-config rule-name)))
  (it "handles else nil"
    (expect-match
      [{:rule-name 'lint/if-let-else-nil
        :form '(if-let binding expr nil)
        :alt '(when-let binding expr)}]
      "(if-let binding expr nil)"
      (single-rule-config rule-name))))
