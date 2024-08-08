; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.locking-object-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'lint/locking-object)

(defn config [& {:as style}]
  (cond-> (single-rule-config rule-name)
    style (update rule-name merge style)))

(defdescribe locking-object-test
  (it "disallows keywords"
    (expect-match
      [{:rule-name rule-name
        :form '(locking :hello (+ 1 1))
        :message "Lock on a symbol bound to (Object.), not a keyword"
        :alt nil}]
      "(locking :hello (+ 1 1))"
      (config)))
  (it "allows symbols"
    (expect-match
      nil
      "(locking hello (+ 1 1))"
      (config))))
