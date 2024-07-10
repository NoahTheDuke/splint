; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.prefer-for-with-literals-test
  (:require
   [lazytest.core :refer [defdescribe it describe]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'style/prefer-for-with-literals)

(defn config [& {:as style}]
  (cond-> (single-rule-config rule-name)
    style (update rule-name merge style)))

(defdescribe prefer-for-with-literals-test
  (it "works with function literals"
    (expect-match
      [{:rule-name rule-name
        :form '(map (splint/fn [%1] (hash-map :a 1 :b %1)) (range 10))
        :message "Prefer `for` when creating a seq of data literals."
        :alt '(for [item (range 10)] {:a 1 :b item})}]
      "(map #(hash-map :a 1 :b %) (range 10))"
      (config)))
  (describe "anonymous functions"
    (it hash-map
      (expect-match
        [{:rule-name rule-name
          :form '(map (fn [x] (hash-map :a 1 :b x)) (range 10))
          :alt '(for [item (range 10)] {:a 1 :b item})}]
        "(map (fn [x] (hash-map :a 1 :b x)) (range 10))"
        (config))))
  (it array-map
    (expect-match
      [{:rule-name rule-name
        :form '(map (fn [x] (array-map :a 1 :b x)) (range 10))
        :alt '(for [item (range 10)] {:a 1 :b item})}]
      "(map (fn [x] (array-map :a 1 :b x)) (range 10))"
      (config)))
  (it hash-set
    (expect-match
      [{:rule-name rule-name
        :form '(map (fn [x] (hash-set :a 1 :b x)) (range 10))
        :alt '(for [item (range 10)] #{item :b :a 1})}]
      "(map (fn [x] (hash-set :a 1 :b x)) (range 10))"
      (config)))
  (it vector
    (expect-match
      [{:rule-name rule-name
        :form '(map (fn [x] (vector :a 1 :b x)) (range 10))
        :alt '(for [item (range 10)] [:a 1 :b item])}]
      "(map (fn [x] (vector :a 1 :b x)) (range 10))"
      (config)))
  (it "ignores threaded contexts"
    (expect-match
      nil
      "(->> (range 10) (map (fn [x] (hash-map :a 1 :b x))))"
      (config))
    (expect-match
      nil
      "(->> [(range 10)] (map (fn [x] (apply hash-map :a 1 :b x))))"
      (config))))
