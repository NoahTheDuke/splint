; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.prefer-for-with-literals-test
  (:require
   [clojure.test :refer [deftest]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(defn config [] (single-rule-config 'style/prefer-for-with-literals))

(deftest prefer-for-with-literals-test
  (expect-match
    [{:rule-name 'style/prefer-for-with-literals
      :form '(map (splint/fn [%1] (hash-map :a 1 :b %1)) (range 10))
      :message "Prefer `for` when creating a seq of data literals."
      :alt '(for [item (range 10)] {:a 1 :b item})}]
    "(map #(hash-map :a 1 :b %) (range 10))"
    (config))
  (expect-match
    [{:rule-name 'style/prefer-for-with-literals
      :form '(map (fn [x] (hash-map :a 1 :b x)) (range 10))
      :alt '(for [item (range 10)] {:a 1 :b item})}]
    "(map (fn [x] (hash-map :a 1 :b x)) (range 10))"
    (config))
  (expect-match
    [{:rule-name 'style/prefer-for-with-literals
      :form '(map (fn [x] (array-map :a 1 :b x)) (range 10))
      :alt '(for [item (range 10)] {:a 1 :b item})}]
    "(map (fn [x] (array-map :a 1 :b x)) (range 10))"
    (config))
  (expect-match
    [{:rule-name 'style/prefer-for-with-literals
      :form '(map (fn [x] (hash-set :a 1 :b x)) (range 10))
      :alt '(for [item (range 10)] #{item :b :a 1})}]
    "(map (fn [x] (hash-set :a 1 :b x)) (range 10))"
    (config))
  (expect-match
    [{:rule-name 'style/prefer-for-with-literals
      :form '(map (fn [x] (vector :a 1 :b x)) (range 10))
      :alt '(for [item (range 10)] [:a 1 :b item])}]
    "(map (fn [x] (vector :a 1 :b x)) (range 10))"
    (config))
  (expect-match
    nil
    "(->> (range 10) (map (fn [x] (hash-map :a 1 :b x))))"
    (config))
  (expect-match
    nil
    "(->> [(range 10)] (map (fn [x] (apply hash-map :a 1 :b x))))"
    (config)))
