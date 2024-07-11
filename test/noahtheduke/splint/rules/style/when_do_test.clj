; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.when-do-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'style/when-do)

(defdescribe when-do-test
  (it "works with variable do args"
    (expect-match
      [{:rule-name rule-name
        :form '(when x (do y))
        :alt '(when x y)}]
      "(when x (do y))"
      (single-rule-config rule-name))
    (expect-match
      [{:form '(when x (do y z))
        :alt '(when x y z)}]
      "(when x (do y z))"
      (single-rule-config rule-name)))
  (it "ignores if when has multiple args"
    (expect-match
      nil
      "(when x y (do z))"
      (single-rule-config rule-name))
    (expect-match
      nil
      "(when x (do y) y)"
      (single-rule-config rule-name))))
