; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.plus-one-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'style/plus-one)

(defn config [& {:as style}]
  (cond-> (single-rule-config rule-name)
    style (update rule-name merge style)))

(defdescribe plus-x-1-test
  (it "understands 1 in either position"
    (expect-match
      [{:alt '(inc x)}]
      "(+ x 1)"
      (config))
    (expect-match
      [{:alt '(inc x)}]
      "(+ 1 x)"
      (config)))
  (it "ignores multi-arity plus"
    (expect-match
      nil
      "(+ x y 1)"
      (config))))
