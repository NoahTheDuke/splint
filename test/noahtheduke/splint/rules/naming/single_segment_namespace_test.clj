; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.naming.single-segment-namespace-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'naming/single-segment-namespace)

(defdescribe single-segment-namespace-test
  (it "works on single segmets" (expect-match
    [{:rule-name rule-name
      :form '(ns simple)
      :message "simple is a single segment. Consider adding an additional segment."
      :alt nil}]
    "(ns simple)"
    (single-rule-config rule-name))
    (expect-match nil "(ns foo.bar)" (single-rule-config rule-name)))

  (it "ignores special cases"
    (expect-match nil "(ns build)" (single-rule-config rule-name))
    (expect-match nil "(ns user)" (single-rule-config rule-name))))
