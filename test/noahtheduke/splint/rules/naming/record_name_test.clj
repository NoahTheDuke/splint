; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.naming.record-name-test
  (:require
    [expectations.clojure.test :refer [defexpect expect]]
    [noahtheduke.splint.test-helpers :refer [check-alt]]))

(defexpect record-name-test
  (expect '(defrecord Foo [a b c])
    (check-alt "(defrecord foo [a b c])"))
  (expect '(defrecord FooBar [a b c])
    (check-alt "(defrecord fooBar [a b c])"))
  (expect '(defrecord Foo-bar [a b c])
    (check-alt "(defrecord foo-bar [a b c])")))
