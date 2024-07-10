; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.prefer-boolean-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'style/prefer-boolean)

(defn config [& {:as style}]
  (cond-> (single-rule-config rule-name)
    style (update rule-name merge style)))

(defdescribe prefer-boolean-test
  (it "works with symbol predicates"
    (expect-match
      [{:rule-name rule-name
        :form '(if some-val true false)
        :alt '(boolean some-val)}]
      "(if some-val true false)"
      (config)))
  (it "works with function predicates"
    (expect-match
      [{:rule-name rule-name
        :form '(if (some-func a b c) true false)
        :alt '(boolean (some-func a b c))}]
      "(if (some-func a b c) true false)"
      (config))))
