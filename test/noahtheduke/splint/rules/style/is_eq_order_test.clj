; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.is-eq-order-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'style/is-eq-order)

(defn config [& {:as style}]
  (cond-> (single-rule-config rule-name)
    style (update rule-name merge style)))

(defdescribe is-eq-order-test
  (it "understands no message"
    (expect-match
      [{:rule-name rule-name
        :form '(is (= status 200))
        :message "Expected value should go first"
        :alt '(is (= 200 status))}]
      "(is (= status 200))"
      (config)))
  (it "understands a message"
    (expect-match
      [{:rule-name rule-name
        :form '(is (= status 200) "message")
        :message "Expected value should go first"
        :alt '(is (= 200 status) "message")}]
      "(is (= status 200) \"message\")"
      (config)))
  (it "ignores no literal"
    (expect-match
      nil
      "(is (= (hash-map :a 1) {:a 1}))"
      (config))
    (expect-match
      nil
      "(is (= (hash-set :a 1) #{:a 1}))"
      (config))))
