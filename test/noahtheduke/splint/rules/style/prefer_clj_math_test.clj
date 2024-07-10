; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.prefer-clj-math-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'style/prefer-clj-math)

(defn config [& {:as style}]
  (cond-> (single-rule-config rule-name)
    style (update rule-name merge style)))

(defdescribe prefer-clj-math-test
  (it "checks function calls"
    (expect-match
      '[{:alt clojure.math/atan}]
      "(Math/atan 45)"
      (config)))
  (it "checks bare symbols"
    (expect-match
      '[{:alt clojure.math/PI}]
      "Math/PI"
      (config)))

  (it "ignores if version is too low"
    (expect-match nil "(Math/atan 45)"
      (assoc (config) :clojure-version {:major 1 :minor 9}))))
