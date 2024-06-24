; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.duplicate-field-name-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(defn config [] (single-rule-config 'lint/duplicate-field-name))

(defdescribe duplicate-field-name-test
  (it "works"
    (expect-match
    [{:rule-name 'lint/duplicate-field-name
      :alt nil
      :message "Duplicate field has been found"}]
    "(defrecord Foo [a b a])"
    (config))))
