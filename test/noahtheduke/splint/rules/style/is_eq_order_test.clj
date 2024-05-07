; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.is-eq-order-test
  (:require
   [expectations.clojure.test :refer [defexpect]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(defn config [] (single-rule-config 'style/is-eq-order))

(defexpect is-eq-order-test
  (expect-match
    [{:rule-name 'style/is-eq-order
      :form '(is (= status 200))
      :message "Expected value should go first"
      :alt '(is (= 200 status))}]
    "(is (= status 200))"
    (config))
  (expect-match
    [{:rule-name 'style/is-eq-order
      :form '(is (= status 200))
      :message "Expected value should go first"
      :alt '(is (= 200 status))}]
    "(is (= status 200))"
    (config))
  (expect-match
    [{:rule-name 'style/is-eq-order
      :form '(is (= status 200) "message")
      :message "Expected value should go first"
      :alt '(is (= 200 status) "message")}]
    "(is (= status 200) \"message\")"
    (config))
  (expect-match
    nil
    "(is (= (hash-map :a 1) {:a 1}))"
    (config))
  (expect-match
    nil
    "(is (= (hash-set :a 1) #{:a 1}))"
    (config)))
