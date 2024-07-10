; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.minus-zero-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'style/minus-zero)

(defn config [& {:as style}]
  (cond-> (single-rule-config rule-name)
    style (update rule-name merge style)))

(defdescribe minus-0-test
  (it "expects 0 to be in final position"
    (expect-match
      [{:rule-name rule-name
        :form '(- x 0)
        :alt 'x}]
      "(- x 0)"
      (config)))
  (it "ignores multi-arity minus"
    (expect-match
      nil
      "(- x y 0)"
      (config))))
