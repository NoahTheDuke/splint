; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.if-not-both-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'lint/if-not-both)

(defdescribe if-not-x-y-x-test
  (it "works"
    (expect-match
      [{:rule-name 'lint/if-not-both
        :form '(if (not x) y z)
        :alt '(if-not x y z)}]
      "(if (not x) y z)"
      (single-rule-config rule-name))))
