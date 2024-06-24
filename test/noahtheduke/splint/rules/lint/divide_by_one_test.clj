; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.divide-by-one-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match]]))

(set! *warn-on-reflection* true)

(defdescribe divide-by-1-test
  (it "works"
    (expect-match
      [{:rule-name 'lint/divide-by-one
        :form '(/ x 1)
        :alt 'x}]
      "(/ x 1)")))
