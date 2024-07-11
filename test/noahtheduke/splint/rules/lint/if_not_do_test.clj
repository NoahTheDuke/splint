; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.if-not-do-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'lint/if-not-do)

(defdescribe if-not-do-test
  (it "handles 2 arity"
    (expect-match
      [{:rule-name 'lint/if-not-do
        :form '(if-not x (do y z))
        :alt '(when-not x y z)}]
      "(if-not x (do y z))"
      (single-rule-config rule-name)))
  (it "handles 3 arity"
    (expect-match
      [{:rule-name 'lint/if-not-do
        :form '(if-not x (do y z) nil)
        :alt '(when-not x y z)}]
      "(if-not x (do y z) nil)"
      (single-rule-config rule-name))))
