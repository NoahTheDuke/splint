; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.if-else-nil-test
  (:require
    [expectations.clojure.test :refer [defexpect expect]]
    [noahtheduke.splint.test-helpers :refer [check-alt]]))

(defexpect if-else-nil-test
  (expect '(when x y) (check-alt "(if x y nil)"))
  (expect '(when x y) (check-alt "(if x (do y))"))
  (expect nil? (check-alt "(if x \"y\" \"z\")")))

(defexpect rest-arg-list-test
  (expect '(when x (if y (z a b c))) (check-alt "(if x (if y (z a b c)))")))
