; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.when-not-do-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'style/when-not-do)

(defn config [& {:as style}]
  (cond-> (single-rule-config rule-name)
    style (update rule-name merge style)))

(defdescribe when-not-do-test
  (it "works with multiple args"
    (expect-match
      [{:alt '(when-not x y z)}]
      "(when-not x (do y z))"
      (config))
    (expect-match
      [{:alt '(when-not x y z)}]
      "(when-not x (do y z) nil)"
      (config))))
