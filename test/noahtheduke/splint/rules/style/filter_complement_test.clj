; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.filter-complement-test
  (:require
    [expectations.clojure.test :refer [defexpect]]
    [noahtheduke.splint.test-helpers :refer [expect-match]]))

(set! *warn-on-reflection* true)

(defexpect filter-complement-test
  (expect-match
    '[{:alt (remove pred coll)}]
    "(filter (complement pred) coll)"))

(defexpect filter-not-pred-test
  (expect-match
    '[{:alt (remove pred coll)}]
    "(filter #(not (pred %)) coll)"))

(defexpect filter-fn*-not-pred-test
  (expect-match
    '[{:alt (remove pred coll)}]
    "(filter (fn* [x] (not (pred x))) coll)"))

(defexpect filter-fn-not-pred-test
  (expect-match
    '[{:alt (remove pred coll)}]
    "(filter (fn [x] (not (pred x))) coll)"))

