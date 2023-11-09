; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.assoc-assoc-test
  (:require
    [expectations.clojure.test :refer [defexpect]]
    [noahtheduke.splint.test-helpers :refer [expect-match]]))

(set! *warn-on-reflection* true)

(defexpect assoc-assoc-key-coll-test
  (expect-match
    '[{:alt (assoc-in coll [:k1 :k2] v)}]
    "(assoc coll :k1 (assoc (:k1 coll) :k2 v))"))

(defexpect assoc-assoc-coll-key-test
  (expect-match
    '[{:alt (assoc-in coll [:k1 :k2] v)}]
    "(assoc coll :k1 (assoc (coll :k1) :k2 v))"))

(defexpect assoc-assoc-get-test
  (expect-match
    '[{:alt (assoc-in coll [:k1 :k2] v)}]
    "(assoc coll :k1 (assoc (get coll :k1) :k2 v))"))
