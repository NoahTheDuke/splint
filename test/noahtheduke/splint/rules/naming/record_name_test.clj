; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.naming.record-name-test
  (:require
   [expectations.clojure.test :refer [defexpect]]
   [noahtheduke.splint.test-helpers :refer [expect-match]]))

(set! *warn-on-reflection* true)

(defexpect record-name-test
  (expect-match
    '[{:alt (defrecord Foo [a b c])}]
    "(defrecord foo [a b c])")
  (expect-match
    '[{:alt (defrecord FooBar [a b c])}]
    "(defrecord fooBar [a b c])")
  (expect-match
    '[{:alt (defrecord FooBar [a b c])}]
    "(defrecord foo-bar [a b c])")
  (expect-match
    '[{:alt (defrecord FooBar [a b c])}]
    "(defrecord Foo-bar [a b c])"))
