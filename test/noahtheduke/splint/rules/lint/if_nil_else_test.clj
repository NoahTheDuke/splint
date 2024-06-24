; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.if-nil-else-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(defn config [] (single-rule-config 'lint/if-let-else-nil))

(defdescribe if-nil-else-test
  (it "matches correctly"
    (expect-match
      [{:rule-name 'lint/if-nil-else
        :form '(if x nil y) 
        :alt '(when-not x y)}]
      "(if x nil y)")))
