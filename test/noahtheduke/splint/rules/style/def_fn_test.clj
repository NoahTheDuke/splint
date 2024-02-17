; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.def-fn-test
  (:require
    [clojure.test :refer [deftest]]
    [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(defn config [] (single-rule-config 'style/def-fn))

(deftest def-let-fn-test
  (expect-match
    [{:form '(def check-inclusion
               (let [allowed #{:a :b :c}]
                 (fn [i] (contains? allowed i))))
      :message "Prefer `let` wrapping `defn`."
      :alt '(let [allowed #{:a :b :c}]
              (defn check-inclusion [i]
                (contains? allowed i)))}]
    "(def check-inclusion (let [allowed #{:a :b :c}] (fn [i] (contains? allowed i))))"
    (config)))

(deftest def-fn-test
  (expect-match
    [{:form '(def some-func (fn [i] (+ i 100)))
      :message "Prefer `defn` instead of `def` wrapping `fn`."
      :alt '(defn some-func [i] (+ i 100))}]
    "(def some-func (fn [i] (+ i 100)))"
    (config)))
