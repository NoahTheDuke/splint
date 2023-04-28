; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.assoc-assoc-test
  (:require
    [expectations.clojure.test :refer [defexpect]]
    [noahtheduke.splint.test-helpers :refer [check-alt]]))

(defexpect assoc-assoc-key-coll-test
  '(assoc-in coll [:k1 :k2] v)
  (check-alt "(assoc coll :k1 (assoc (:k1 coll) :k2 v))"))

(defexpect assoc-assoc-coll-key-test
  '(assoc-in coll [:k1 :k2] v)
  (check-alt "(assoc coll :k1 (assoc (coll :k1) :k2 v))"))

(defexpect assoc-assoc-get-test
  '(assoc-in coll [:k1 :k2] v)
  (check-alt "(assoc coll :k1 (assoc (get coll :k1) :k2 v))"))
