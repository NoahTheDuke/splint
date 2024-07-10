; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.naming.single-segment-namespace-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'naming/single-segment-namespace)

(defn config [& {:as style}]
  (cond-> (single-rule-config rule-name)
    style (update rule-name merge style)))

(defdescribe single-segment-namespace-test
  (it "works on single segmets" (expect-match
    [{:rule-name rule-name
      :form '(ns simple)
      :message "simple is a single segment. Consider adding an additional segment."
      :alt nil}]
    "(ns simple)"
    (config))
    (expect-match nil "(ns foo.bar)" (config)))

  (it "ignores special cases"
    (expect-match nil "(ns build)" (config))
    (expect-match nil "(ns user)" (config))))
