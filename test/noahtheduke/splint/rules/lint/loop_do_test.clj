; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.loop-do-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(defn config [] (single-rule-config 'lint/loop-do))

(defdescribe loop-do-test
  (it "works"
    (expect-match
      [{:rule-name 'lint/loop-do
        :form '(loop [] (do a b))
        :alt '(loop [] a b)}]
      "(loop [] (do a b))"
      (config))))
