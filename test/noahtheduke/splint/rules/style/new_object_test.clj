; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.new-object-test
  (:require
   [expectations.clojure.test :refer [defexpect]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(defn config [] (single-rule-config 'style/new-object))

(defn method-config []
  (-> (config)
    (assoc :clojure-version {:major 1 :minor 12})
    (assoc-in '[style/new-object :chosen-style] :method-value)))

(defexpect new-object-test
  (expect-match
    [{:rule-name 'style/new-object
      :form '(new java.util.ArrayList 100)
      :alt '(java.util.ArrayList. 100)
      :message "Foo. is preferred."}]
    "(new java.util.ArrayList 100)"
    (config))
  (expect-match
    [{:rule-name 'style/new-object
      :form '(new java.util.ArrayList 100)
      :alt '(java.util.ArrayList. 100)
      :message "Foo. is preferred."}]
    "(new java.util.ArrayList 100)"
    (-> (method-config)
      (assoc :clojure-version {:major 1 :minor 11})))
  (expect-match
    [{:rule-name 'style/new-object
      :form '(new java.util.ArrayList 100)
      :alt '(java.util.ArrayList/new 100)
      :message "Foo/new is preferred."}]
    "(new java.util.ArrayList 100)"
    (method-config))
  (expect-match
    [{:rule-name 'style/new-object
      :form '(java.util.ArrayList. 100)
      :alt '(java.util.ArrayList/new 100)
      :message "Foo/new is preferred."}]
    "(java.util.ArrayList. 100)"
    (method-config))
  (expect-match
    nil
    "(java.util.ArrayList. 100)"
    (config)))
