; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.def-fn-test
  (:require
    [expectations.clojure.test :refer [defexpect expect]]
    [noahtheduke.splint-test :refer [check-alt]]))

(defexpect def-let-fn-test
  (expect '(let [allowed #{:a :b :c}] (defn check-inclusion [i] (contains? allowed i)))
    (check-alt "(def check-inclusion (let [allowed #{:a :b :c}] (fn [i] (contains? allowed i))))")))

(defexpect def-fn-test
  (expect '(defn some-func [i] (+ i 100))
    (check-alt "(def some-func (fn [i] (+ i 100)))")))
