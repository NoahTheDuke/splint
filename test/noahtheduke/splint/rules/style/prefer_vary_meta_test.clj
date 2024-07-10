; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.prefer-vary-meta-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'style/prefer-vary-meta)

(defn config [& {:as style}]
  (cond-> (single-rule-config rule-name)
    style (update rule-name merge style)))

(defdescribe prefer-vary-meta-test
  (it "works"
    (expect-match
      [{:alt '(vary-meta x f args)}]
      "(with-meta x (f (meta x) args))"
      (config)))
  (it "ignores direct non-meta calls"
    (expect-match
      nil
      "(let [xm (meta x)] (with-meta x (f xm args)))"
      (config))))
