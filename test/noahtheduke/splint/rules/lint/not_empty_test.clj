; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.not-empty-test
  (:require
    [expectations.clojure.test :refer [defexpect]]
    [noahtheduke.splint.test-helpers :refer [expect-match]]))

(set! *warn-on-reflection* true)

(defexpect not-empty?-test
  (expect-match
    '[{:alt (seq x)}]
    "(not (empty? x))"))

(defexpect not-empty?-not-empty-style-test
  (expect-match
    '[{:alt (not-empty x)}]
    "(not (empty? x))"
    '{lint/not-empty? {:chosen-style :not-empty}}))
