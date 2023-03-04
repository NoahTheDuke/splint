; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.parser-test
  (:require
    [expectations.clojure.test :refer [defexpect]]
    [noahtheduke.splint.parser :refer [parse-string-all]]))

(defexpect unknown-tagged-literals-test
  '[(sql/raw [1 2 3]) (splint/unknown [4])]
  (parse-string-all "#sql/raw [1 2 3] #unknown [4]"))
