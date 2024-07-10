; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.multiple-arity-order-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'style/multiple-arity-order)

(defn config [& {:as style}]
  (cond-> (single-rule-config rule-name)
    style (update rule-name merge style)))

(defdescribe multiple-arity-order-test
  (it "works"
    (expect-match
      [{:form '(defn foo
                 ([x] (foo x 1))
                 ([x y & more] (reduce foo (+ x y) more))
                 ([x y] (+ x y)))
        :message "defn arities should be sorted fewest to most arguments."
        :alt '(defn foo
                ([x] (foo x 1))
                ([x y] (+ x y))
                ([x y & more] (reduce foo (+ x y) more)))}]
      "(defn foo
         ([x] (foo x 1))
         ([x y & more] (reduce foo (+ x y) more))
         ([x y] (+ x y)))"
      (config)))
  (it "ignores incorrect defns"
    (expect-match
      nil
      "(defn foo ([a] 1) [a b])"
      (config))))
