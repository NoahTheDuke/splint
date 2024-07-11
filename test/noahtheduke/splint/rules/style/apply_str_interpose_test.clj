; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.apply-str-interpose-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'style/apply-str-interpose)

(defdescribe str-apply-interpose-test
  (it "works"
    (expect-match
      [{:rule-name rule-name
        :form '(apply str (interpose x y))
        :alt '(clojure.string/join x y)}]
      "(apply str (interpose x y))"
      (single-rule-config rule-name))))
