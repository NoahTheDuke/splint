; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.into-literal-test
  (:require
    [expectations.clojure.test :refer [defexpect]]
    [noahtheduke.splint.test-helpers :refer [check-alt]]))

(defexpect into-vec-test
  '(vec coll)
  (check-alt "(into [] coll)"))

(defexpect into-set-test
  '(set coll)
  (check-alt "(into #{} coll)"))
