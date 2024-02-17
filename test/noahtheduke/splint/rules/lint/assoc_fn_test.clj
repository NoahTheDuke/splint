; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.assoc-fn-test
  (:require
    [clojure.test :refer [deftest]]
    [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(defn config [] (single-rule-config 'lint/assoc-fn))

(deftest assoc-fn-key-coll-test
  (expect-match
    '[{:alt (update coll :k f arg)}]
    "(assoc coll :k (f (:k coll) arg))"
    (config)))

(deftest assoc-fn-coll-key-test
  (expect-match
    '[{:alt (update coll :k f arg1 arg2)}]
    "(assoc coll :k (f (coll :k) arg1 arg2))"
    (config)))

(deftest assoc-fn-get-test
  (expect-match
    [{:form '(assoc coll :k (f (get coll :k) arg1 arg2 arg3))
      :message "Use `update` instead of recreating it."
      :alt '(update coll :k f arg1 arg2 arg3)}]
    "(assoc coll :k (f (get coll :k) arg1 arg2 arg3))"
    (config)))
