; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.dorun-map-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'lint/dorun-map)

(defdescribe dorun-map-test
  (it "works"
    (expect-match
      [{:rule-name 'lint/dorun-map
        :form '(dorun (map f coll))
        :alt '(run! f coll)}]
      "(dorun (map f coll))"
      (single-rule-config rule-name))))
