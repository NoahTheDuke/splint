; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.multiply-by-zero-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'style/multiply-by-zero)

(defn config [& {:as style}]
  (cond-> (single-rule-config rule-name)
    style (update rule-name merge style)))

(defdescribe multiply-by-1-test
  (it "works in either order"
    (expect-match [{:alt 0}] "(* x 0)" (config))
    (expect-match [{:alt 0}] "(* 0 x)" (config)))
  (it "ignores multi-arity multiply"
    (expect-match nil "(* x y 0)" (config))
    (expect-match nil "(* 0 x y)" (config))))
