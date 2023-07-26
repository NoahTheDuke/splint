; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.path-matcher-test
  (:require
    [expectations.clojure.test :refer [defexpect expect]]
    [noahtheduke.splint.path-matcher :as sut]
    [clojure.java.io :as io]))

(defexpect path-matcher-test
  (expect (sut/matches (sut/->matcher "glob:foo") (io/file "foo")))
  (expect (sut/matches (sut/->matcher "glob:foo") (.toPath (io/file "foo")))))
