; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.performance.single-literal-merge-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'performance/single-literal-merge)

(defdescribe single-literal-merge-test
  (it "flattens given map"
    (expect-match
      [{:rule-name 'performance/single-literal-merge
        :form '(merge m {:a 1 :b 2})
        :message "Prefer assoc for merging literal maps"
        :alt '(assoc m :a 1 :b 2)}]
      "(merge m {:a 1 :b 2})"
      (single-rule-config rule-name)))

  (it "works on nil"
    (expect-match
      [{:rule-name 'performance/single-literal-merge
        :form '(merge nil {:a 1 :b 2})
        :message "Prefer assoc for merging literal maps"
        :alt '(assoc nil :a 1 :b 2)}]
      "(merge nil {:a 1 :b 2})"
      (single-rule-config rule-name)))

  (it "respects :chosen-style :multiple"
    (expect-match
      [{:rule-name 'performance/single-literal-merge
        :form '(merge m {:a 1 :b 2})
        :message "Prefer assoc for merging literal maps"
        :alt '(-> m
                  (assoc :a 1)
                  (assoc :b 2))}]
      "(merge m {:a 1 :b 2})"
      (single-rule-config rule-name {:chosen-style :multiple})))

  (it "it respects performance/assoc-many"
    (expect-match
      [{:rule-name 'performance/single-literal-merge
        :form '(merge m {:a 1 :b 2})
        :message "Prefer assoc for merging literal maps"
        :alt '(-> m
                  (assoc :a 1)
                  (assoc :b 2))}]
      "(merge m {:a 1 :b 2})"
      (update (single-rule-config rule-name) 'performance/assoc-many assoc :enabled true)))

  (it "keeps the order in the alt"
    (expect-match
      [{:rule-name 'performance/single-literal-merge
        :form '(merge a {{:x :y} :a {:foo :bar} :b})
        :message "Prefer assoc for merging literal maps"
        :alt '(assoc a {:x :y} :a {:foo :bar} :b)}]
      "(merge a {{:x :y} :a {:foo :bar} :b})"
      (single-rule-config rule-name))
    (expect-match
      [{:rule-name 'performance/single-literal-merge
        :form '(merge a {:a :b :c :d :e :f :g :h :i :j :k :l :m
                         :n :o :p :q :r :s :t :u :v :w :x :y :z})
        :message "Prefer assoc for merging literal maps"
        :alt '(assoc a :a :b :c :d :e :f :g :h :i :j :k :l :m :n :o :p :q :r :s :t :u :v :w :x :y :z)}]
      "(merge a {:a :b :c :d :e :f :g :h :i :j :k :l :m :n :o :p :q :r :s :t :u :v :w :x :y :z})"
      (single-rule-config rule-name)))

  (it "ignores multiple maps"
    (expect-match
      nil
      "(merge m {:a 1 :b 2} {:c 3 :d 4})"
      (single-rule-config rule-name)))

  (it "ignores empty map literals"
    (expect-match
      nil
      "(merge m {})"
      (single-rule-config rule-name)))

  (it "ignores multiple symbols"
    (expect-match
      nil
      "(merge m b)"
      (single-rule-config rule-name))))
