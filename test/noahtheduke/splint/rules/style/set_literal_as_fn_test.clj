; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.set-literal-as-fn-test
  (:require
   [expectations.clojure.test :refer [defexpect]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(defn config [] (single-rule-config 'style/set-literal-as-fn))

(defexpect set-literal-as-fn-test
  (expect-match
    [{:rule-name 'style/set-literal-as-fn
      :form '(#{'a 'b 'c} elem)
      :message "Prefer `case` to set literal with constant members."
      :alt '(case elem (a b c) elem nil)}]
    "(#{'a 'b 'c} elem)"
    (config))
  (expect-match
    [{:rule-name 'style/set-literal-as-fn
      :form '(#{nil 1 :b 'c} elem)
      :message "Prefer `case` to set literal with constant members."
      :alt '(case elem (nil 1 :b c) elem nil)}]
    "(#{nil 1 :b 'c} elem)"
    (config))
  (expect-match nil "(#{'a 'b c} elem)" (config))
  (expect-match nil "(#{'a 'b 'c '(1 2 3)} elem)" (config)))
