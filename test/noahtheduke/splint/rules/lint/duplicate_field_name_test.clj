; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.duplicate-field-name-test
  (:require
    [expectations.clojure.test :refer [defexpect]]
    [noahtheduke.splint-test :refer [check-str]]))

(defexpect duplicate-field-name-test
  '"Duplicate field has been found"
  (:message (first (check-str "(defrecord Foo [a b a])"))))
