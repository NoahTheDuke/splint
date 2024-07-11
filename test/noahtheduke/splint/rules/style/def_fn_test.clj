; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.def-fn-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'style/def-fn)

(defdescribe def-let-fn-test
  (it "finds fn in let in def"
    (expect-match
      [{:form '(def check-inclusion
                 (let [allowed #{:a :b :c}]
                   (fn [i] (contains? allowed i))))
        :message "Prefer `let` wrapping `defn`."
        :alt '(let [allowed #{:a :b :c}]
                (defn check-inclusion [i]
                  (contains? allowed i)))}]
      "(def check-inclusion (let [allowed #{:a :b :c}] (fn [i] (contains? allowed i))))"
      (single-rule-config rule-name)))

  (it "finds fn in def"
    (expect-match
      [{:form '(def some-func (fn [i] (+ i 100)))
        :message "Prefer `defn` instead of `def` wrapping `fn`."
        :alt '(defn some-func [i] (+ i 100))}]
      "(def some-func (fn [i] (+ i 100)))"
      (single-rule-config rule-name))))
