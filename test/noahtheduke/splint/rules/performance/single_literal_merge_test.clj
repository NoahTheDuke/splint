; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.performance.single-literal-merge-test
  (:require
   [expectations.clojure.test :refer [defexpect]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(defn config [] (single-rule-config 'performance/single-literal-merge))

(defexpect single-literal-merge-test
  (expect-match
    [{:rule-name 'performance/single-literal-merge
      :form '(merge m {:a 1 :b 2})
      :message "Prefer assoc for merging literal maps"
      :alt '(assoc m :a 1 :b 2)}]
    "(merge m {:a 1 :b 2})"
    (config)))

(defexpect single-literal-merge-nil-test
  (expect-match
    [{:rule-name 'performance/single-literal-merge
      :form '(merge nil {:a 1 :b 2})
      :message "Prefer assoc for merging literal maps"
      :alt '(assoc nil :a 1 :b 2)}]
    "(merge nil {:a 1 :b 2})"
    (config)))

(defexpect single-literal-merge-style-multiple-test
  (expect-match
    [{:rule-name 'performance/single-literal-merge
      :form '(merge m {:a 1 :b 2})
      :message "Prefer assoc for merging literal maps"
      :alt '(-> m
              (assoc :a 1)
              (assoc :b 2))}]
    "(merge m {:a 1 :b 2})"
    (update (config) 'performance/single-literal-merge assoc :chosen-style :multiple)))

(defexpect single-literal-merge-style-interaction-with-assoc-many-test
  (expect-match
    [{:rule-name 'performance/single-literal-merge
      :form '(merge m {:a 1 :b 2})
      :message "Prefer assoc for merging literal maps"
      :alt '(-> m
              (assoc :a 1)
              (assoc :b 2))}]
    "(merge m {:a 1 :b 2})"
    (update (config) 'performance/assoc-many assoc :enabled true)))

(defexpect single-literal-merge-sort-test
  (expect-match
    [{:rule-name 'performance/single-literal-merge
      :form '(merge a {{:x :y} :a {:foo :bar} :b})
      :message "Prefer assoc for merging literal maps"
      :alt '(assoc a {:x :y} :a {:foo :bar} :b)}]
    "(merge a {{:x :y} :a {:foo :bar} :b})"
    (config))
  (expect-match
    [{:rule-name 'performance/single-literal-merge
      :form '(merge a {:a :b :c :d :e :f :g :h :i :j :k :l :m
                       :n :o :p :q :r :s :t :u :v :w :x :y :z})
      :message "Prefer assoc for merging literal maps"
      :alt '(assoc a :a :b :c :d :e :f :g :h :i :j :k :l :m :n :o :p :q :r :s :t :u :v :w :x :y :z)}]
    "(merge a {:a :b :c :d :e :f :g :h :i :j :k :l :m :n :o :p :q :r :s :t :u :v :w :x :y :z})"
    (config)))

(defexpect single-literal-merge-multiples-test
  (expect-match
    nil
    "(merge m {:a 1 :b 2} {:c 3 :d 4})"
    (config)))

(defexpect single-literal-merge-empty-test
  (expect-match
    nil
    "(merge m {})"
    (config)))

(defexpect single-literal-merge-symbol-test
  (expect-match
    nil
    "(merge m b)"
    (config)))
