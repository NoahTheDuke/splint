; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.naming.record-name-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'naming/record-name)

(defn config [& {:as style}]
  (cond-> (single-rule-config rule-name)
    style (update rule-name merge style)))

(defdescribe record-name-test
  (it "handles all cases"
    (expect-match
      [{:rule-name rule-name
        :form '(defrecord foo [a b c])
        :alt '(defrecord Foo [a b c])}]
      "(defrecord foo [a b c])"
      (config))
    (expect-match
      [{:rule-name rule-name
        :form '(defrecord fooBar [a b c])
        :alt '(defrecord FooBar [a b c])}]
      "(defrecord fooBar [a b c])"
      (config))
    (expect-match
      [{:rule-name rule-name
        :form '(defrecord foo-bar [a b c])
        :alt '(defrecord FooBar [a b c])}]
      "(defrecord foo-bar [a b c])"
      (config))
    (expect-match
      [{:rule-name rule-name
        :form '(defrecord Foo-bar [a b c])
        :alt '(defrecord FooBar [a b c])}]
      "(defrecord Foo-bar [a b c])"
      (config))))
