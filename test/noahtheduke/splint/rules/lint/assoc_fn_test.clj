; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.assoc-fn-test
  (:require
    [expectations.clojure.test :refer [defexpect]]
    [noahtheduke.splint.test-helpers :refer [expect-match]]))

(defexpect assoc-fn-key-coll-test
  (expect-match
    '[{:alt (update coll :k f args)}]
    "(assoc coll :k (f (:k coll) args))"))

(defexpect assoc-fn-coll-key-test
  (expect-match
    '[{:alt (update coll :k f args)}]
    "(assoc coll :k (f (coll :k) args))"))

(defexpect assoc-fn-get-test
  (expect-match
    '[{:alt (update coll :k f args)}]
    "(assoc coll :k (f (get coll :k) args))"))
