; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.empty-loop-in-fn-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'lint/empty-loop-in-fn)

(defdescribe empty-loop-in-fn-test
  (it "works with no arg wrapping"
    (expect-match
      [{:rule-name rule-name
        :form '(defn cool [] (loop [] (+ 1 1)))
        :message "Empty loop can be removed for direct recursion"
        :alt '(defn cool [] (+ 1 1))}]
      "(defn cool [] (loop [] (+ 1 1)))"
      (single-rule-config rule-name)))
  (it "works with arg wrapping"
    (expect-match
      [{:rule-name rule-name
        :form '(defn cool ([] (loop [] (+ 1 1))))
        :message "Empty loop can be removed for direct recursion"
        :alt '(defn cool ([] (+ 1 1)))}]
      "(defn cool ([] (loop [] (+ 1 1))))"
      (single-rule-config rule-name))))
