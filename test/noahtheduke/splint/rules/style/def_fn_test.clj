; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.def-fn-test
  (:require
    [expectations.clojure.test :refer [defexpect]]
    [noahtheduke.splint.test-helpers :refer [expect-match]]))

(defexpect def-let-fn-test
  (expect-match
    '[{:alt (let [allowed #{:a :b :c}] (defn check-inclusion [i] (contains? allowed i)))}]
    "(def check-inclusion (let [allowed #{:a :b :c}] (fn [i] (contains? allowed i))))"))

(defexpect def-fn-test
  (expect-match
    '[{:alt (defn some-func [i] (+ i 100))}]
    "(def some-func (fn [i] (+ i 100)))"))
