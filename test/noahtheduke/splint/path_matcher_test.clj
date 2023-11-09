; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.path-matcher-test
  (:require
    [noahtheduke.splint.test-helpers]
    [clojure.java.io :as io]
    [expectations.clojure.test :refer [defexpect expect]]
    [matcher-combinators.test :refer [match?]]
    [noahtheduke.splint.path-matcher :as sut]))

(set! *warn-on-reflection* true)

(defexpect path-matcher-test
  (expect (sut/matches (sut/->matcher "glob:foo") (io/file "foo")))
  (expect (sut/matches (sut/->matcher "glob:foo") (.toPath (io/file "foo"))))
  (expect (match? (sut/->matcher "glob:foo") (sut/->matcher "foo"))))
