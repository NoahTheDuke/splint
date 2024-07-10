; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.set-literal-as-fn-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'style/set-literal-as-fn)

(defn config [& {:as style}]
  (cond-> (single-rule-config rule-name)
    style (update rule-name merge style)))

(defdescribe set-literal-as-fn-test
  (it "plain strings"
    (expect-match
      [{:rule-name rule-name
        :form '(#{'a 'b 'c} elem)
        :message "Prefer `case` to set literal with constant members."
        :alt '(case elem (a b c) elem nil)}]
      "(#{'a 'b 'c} elem)"
      (config)))
  (it "other constant types"
    (expect-match
      [{:rule-name rule-name
        :form '(#{nil 1 :b 'c} elem)
        :message "Prefer `case` to set literal with constant members."
        :alt '(case elem (nil 1 :b c) elem nil)}]
      "(#{nil 1 :b 'c} elem)"
      (config)))
  (it "ignores if not all elements are quoted"
    (expect-match nil "(#{'a 'b c} elem)" (config)))
  (it "ignores if element can't be treated as constant"
    (expect-match nil "(#{'a 'b 'c '(1 2 3)} elem)" (config))))
