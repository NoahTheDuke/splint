; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.filter-complement-test
  (:require
    [expectations.clojure.test :refer [defexpect]]
    [noahtheduke.splint-test :refer [check-alt]]))

(defexpect filter-complement-test
  '(remove pred coll)
  (check-alt "(filter (complement pred) coll)"))

(defexpect filter-not-pred-test
  '(remove pred coll)
  (check-alt "(filter #(not (pred %)) coll)"))

(defexpect filter-fn*-not-pred-test
  '(remove pred coll)
  (check-alt "(filter (fn* [x] (not (pred x))) coll)"))

(defexpect filter-fn-not-pred-test
  '(remove pred coll)
  (check-alt "(filter (fn [x] (not (pred x))) coll)"))

