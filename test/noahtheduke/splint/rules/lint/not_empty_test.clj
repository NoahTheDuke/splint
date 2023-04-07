; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.not-empty-test
  (:require
    [expectations.clojure.test :refer [defexpect]]
    [noahtheduke.splint-test :refer [check-alt]]))

(defexpect not-empty?-test
  '(seq x)
  (check-alt "(not (empty? x))"))

(defexpect not-empty?-not-empty-style-test
  '(not-empty x)
  (check-alt "(not (empty? x))" '{lint/not-empty? {:chosen-style :not-empty}}))
