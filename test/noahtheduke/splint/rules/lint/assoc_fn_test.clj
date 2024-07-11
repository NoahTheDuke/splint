; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.assoc-fn-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'lint/assoc-fn)

(defdescribe assoc-fn-test

  (it "respects key-coll"
    (expect-match
      [{:rule-name 'lint/assoc-fn
        :form '(assoc coll :k (f (:k coll) arg))
        :alt '(update coll :k f arg)}]
      "(assoc coll :k (f (:k coll) arg))"
      (single-rule-config rule-name))
    (expect-match
      [{:rule-name 'lint/assoc-fn
        :form '(clojure.core/assoc coll :k (f (:k coll) arg))
        :alt '(update coll :k f arg)}]
      "(clojure.core/assoc coll :k (f (:k coll) arg))"
      (single-rule-config rule-name)))

  (it "respects coll-key"
    (expect-match
      [{:rule-name 'lint/assoc-fn
        :form '(assoc coll :k (f (coll :k) arg1 arg2))
        :alt '(update coll :k f arg1 arg2)}]
      "(assoc coll :k (f (coll :k) arg1 arg2))"
      (single-rule-config rule-name)))

  (it "respects get coll key"
    (expect-match
      [{:rule-name 'lint/assoc-fn
        :form '(assoc coll :k (f (get coll :k) arg1 arg2 arg3))
        :message "Use `update` instead of recreating it."
        :alt '(update coll :k f arg1 arg2 arg3)}]
      "(assoc coll :k (f (get coll :k) arg1 arg2 arg3))"
      (single-rule-config rule-name)))

  (it "gracefully handles misses"
    (expect-match
      nil
      "(assoc coll :k (assoc (:k coll) arg))"
      (single-rule-config rule-name))
    ;; https://github.com/NoahTheDuke/splint/issues/15
    (expect-match
      nil
      "(assoc x :a (or (:a x) y))"
      (single-rule-config rule-name))))
