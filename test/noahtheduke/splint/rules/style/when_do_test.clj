; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.when-do-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'style/when-do)

(defn config [& {:as style}]
  (cond-> (single-rule-config rule-name)
    style (update rule-name merge style)))

(defdescribe when-do-test
  (it "works with variable do args"
    (expect-match
      [{:rule-name rule-name
        :form '(when x (do y))
        :alt '(when x y)}]
      "(when x (do y))"
      (config))
    (expect-match
      [{:form '(when x (do y z))
        :alt '(when x y z)}]
      "(when x (do y z))"
      (config)))
  (it "ignores if when has multiple args"
    (expect-match
      nil
      "(when x y (do z))"
      (config))
    (expect-match
      nil
      "(when x (do y) y)"
      (config))))
